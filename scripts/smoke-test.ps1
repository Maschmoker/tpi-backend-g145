$ErrorActionPreference = 'Stop'

$base = 'http://localhost:8080'
$kc   = 'http://localhost:8088'
$corr = [guid]::NewGuid().ToString()

$password = $env:KEYCLOAK_TEST_USER_PASSWORD
if ([string]::IsNullOrWhiteSpace($password)) { $password = 'Test1234!' }

function Wait-Health($name, $port) {
    Write-Host "Esperando health de $name (puerto $port)..."
    for ($i = 0; $i -lt 90; $i++) {
        try {
            $r = Invoke-WebRequest -UseBasicParsing -Uri "http://localhost:$port/actuator/health" -TimeoutSec 3
            if ($r.StatusCode -eq 200) { Write-Host "  OK $name"; return }
        } catch { }
        Start-Sleep -Seconds 2
    }
    throw "Timeout esperando health de $name"
}

function Get-Token($user) {
    $body = @{
        grant_type = 'password'
        client_id  = 'guarderia-swagger'
        username   = $user
        password   = $password
    }
    $r = Invoke-RestMethod -Method Post -Uri "$kc/realms/guarderia/protocol/openid-connect/token" -Body $body
    return $r.access_token
}

function AuthHeaders($token) {
    $h = @{ 'X-Correlation-ID' = $corr }
    if ($token) { $h['Authorization'] = "Bearer $token" }
    return $h
}

function Call($method, $uri, $token, $body = $null) {
    $params = @{ Method = $method; Uri = $uri; Headers = (AuthHeaders $token); ContentType = 'application/json' }
    if ($null -ne $body) { $params['Body'] = ($body | ConvertTo-Json -Depth 10) }
    return Invoke-RestMethod @params
}

# 0. Health
foreach ($svc in @(@{n='gateway-service';p=8080},@{n='clientes-service';p=8081},@{n='estadias-service';p=8082},@{n='logistica-service';p=8083})) {
    Wait-Health $svc.n $svc.p
}

# 1. Tokens
$tokCliente      = Get-Token 'cliente1'
$tokRecepcion    = Get-Token 'recepcionista1'
$tokCuidado      = Get-Token 'cuidado1'
$tokChofer       = Get-Token 'chofer1'
$tokAdmin        = Get-Token 'admin1'
Write-Host "Tokens obtenidos. Correlation-ID: $corr"

# 2. Cliente actualiza perfil
$perfil = @{ nombre='Cliente'; apellido='Smoke'; email='cliente1@guarderia.test'; telefono='123' }
Call 'Put' "$base/api/clientes/me" $tokCliente $perfil | Out-Null
Write-Host "Perfil cliente actualizado"

# 3. Crear direccion con coordenadas
$dirBody = @{ calle='Av. Colón'; numero='123'; barrio='Centro'; ciudad='Córdoba'; latitud=-31.42; longitud=-64.18; esPrincipal=$true }
$dir = Call 'Post' "$base/api/clientes/me/direcciones" $tokCliente $dirBody
$dirId = $dir.id
Write-Host "Direccion creada id=$dirId"

# 4. Crear mascota
$mascotaBody = @{ nombre='Firulais'; especie='PERRO'; raza='Mestizo'; tamanio='MEDIANO'; pesoKg=12.5; observaciones='smoke' }
$mascota = Call 'Post' "$base/api/clientes/me/mascotas" $tokCliente $mascotaBody
$mascotaId = $mascota.id
Write-Host "Mascota creada id=$mascotaId"

# 5. Catalogo (admin)
$catalogo = Call 'Get' "$base/api/estadias/catalogo" $tokAdmin
$servicioId = $catalogo[0].id
Write-Host "Servicio catalogo id=$servicioId"

$fechaIngreso  = (Get-Date).AddDays(1).ToString('yyyy-MM-dd')
$fechaEgreso   = (Get-Date).AddDays(2).ToString('yyyy-MM-dd')

# 6. Cotizar
$cotBody = @{ mascotaId=$mascotaId; modalidad='CON_TRASLADO'; requiereRetiro=$true; requiereDevolucion=$true; direccionTrasladoId=$dirId; fechaIngresoPlanificada=$fechaIngreso; fechaEgresoPlanificada=$fechaEgreso; serviciosCatalogoIds=@($servicioId); observaciones='smoke' }
$cot = Call 'Post' "$base/api/estadias/cotizar" $tokCliente $cotBody
Write-Host "Cotizacion total=$($cot.costoTotal)"

# 7. Crear estadia
$est = Call 'Post' "$base/api/estadias" $tokCliente $cotBody
$estadiaId = $est.id
Write-Host "Estadia creada id=$estadiaId estado=$($est.estado)"

# 8. Generar ruta RETIRO
$rutaRetiroReq = @{ fecha=$fechaIngreso; tipo='RETIRO' }
$rutaRetiro = Call 'Post' "$base/api/logistica/rutas/generar" $tokAdmin $rutaRetiroReq
$rutaRetiroId = $rutaRetiro.id
Write-Host "Ruta RETIRO generada id=$rutaRetiroId"

# 9. Chofer inicia ruta retiro -> estadia EN_TRANSITO_IDA
Call 'Put' "$base/api/logistica/rutas/$rutaRetiroId/iniciar" $tokChofer | Out-Null
$e = Call 'Get' "$base/api/estadias/$estadiaId" $tokChofer
Write-Host "Tras iniciar ruta retiro: estadia=$($e.estado)"
if ($e.estado -ne 'EN_TRANSITO_IDA') { throw "Se esperaba EN_TRANSITO_IDA, fue $($e.estado)" }

# 10. Completar todas las paradas de la ruta retiro -> estadia RECIBIDA
foreach ($p in ($rutaRetiro.paradas | Sort-Object orden)) {
    Call 'Put' "$base/api/logistica/rutas/$rutaRetiroId/paradas/$($p.orden)/completar" $tokChofer | Out-Null
}
$e = Call 'Get' "$base/api/estadias/$estadiaId" $tokChofer
Write-Host "Tras completar paradas retiro: estadia=$($e.estado)"
if ($e.estado -ne 'RECIBIDA') { throw "Se esperaba RECIBIDA, fue $($e.estado)" }

# 11. Recepcionista inicia cuidado
Call 'Patch' "$base/api/estadias/$estadiaId/iniciar-cuidado" $tokRecepcion | Out-Null
$e = Call 'Get' "$base/api/estadias/$estadiaId" $tokRecepcion
Write-Host "Tras iniciar cuidado: estadia=$($e.estado)"
if ($e.estado -ne 'EN_CUIDADO') { throw "Se esperaba EN_CUIDADO, fue $($e.estado)" }

# 12. Cuidado: iniciar y realizar cada servicio (auto-transicion a LISTA_PARA_RETIRO)
$servicios = Call 'Get' "$base/api/estadias/$estadiaId/servicios" $tokCuidado
foreach ($s in $servicios) {
    Call 'Patch' "$base/api/estadias/$estadiaId/servicios/$($s.id)/iniciar" $tokCuidado | Out-Null
    Call 'Patch' "$base/api/estadias/$estadiaId/servicios/$($s.id)/realizar" $tokCuidado | Out-Null
}
$e = Call 'Get' "$base/api/estadias/$estadiaId" $tokAdmin
Write-Host "Tras realizar servicios: estadia=$($e.estado)"
if ($e.estado -ne 'LISTA_PARA_RETIRO') { throw "Se esperaba LISTA_PARA_RETIRO, fue $($e.estado)" }

# 13. Generar ruta DEVOLUCION
$rutaDevReq = @{ fecha=$fechaEgreso; tipo='DEVOLUCION' }
$rutaDev = Call 'Post' "$base/api/logistica/rutas/generar" $tokAdmin $rutaDevReq
$rutaDevId = $rutaDev.id
Write-Host "Ruta DEVOLUCION generada id=$rutaDevId"

# 14. Chofer inicia ruta devolucion -> EN_TRANSITO_VUELTA
Call 'Put' "$base/api/logistica/rutas/$rutaDevId/iniciar" $tokChofer | Out-Null
$e = Call 'Get' "$base/api/estadias/$estadiaId" $tokChofer
Write-Host "Tras iniciar ruta devolucion: estadia=$($e.estado)"
if ($e.estado -ne 'EN_TRANSITO_VUELTA') { throw "Se esperaba EN_TRANSITO_VUELTA, fue $($e.estado)" }

# 15. Completar todas las paradas de la ruta devolucion -> ENTREGADA
foreach ($p in ($rutaDev.paradas | Sort-Object orden)) {
    Call 'Put' "$base/api/logistica/rutas/$rutaDevId/paradas/$($p.orden)/completar" $tokChofer | Out-Null
}
$e = Call 'Get' "$base/api/estadias/$estadiaId" $tokChofer
Write-Host "Tras completar paradas devolucion: estadia=$($e.estado)"
if ($e.estado -ne 'ENTREGADA') { throw "Se esperaba ENTREGADA, fue $($e.estado)" }

Write-Host ""
Write-Host "SMOKE TEST OK - Correlation-ID: $corr" -ForegroundColor Green