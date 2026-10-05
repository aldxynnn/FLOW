$ErrorActionPreference = "Stop"

$ProjectRoot = Split-Path -Parent $PSScriptRoot
$BackendDir  = Join-Path $ProjectRoot "backend\flow-backend"
$WebDir      = Join-Path $ProjectRoot "web"

Write-Host ""
Write-Host "========================================"
Write-Host " FLOW DEVELOPMENT"
Write-Host "========================================"
Write-Host ""

# Safe local development defaults.
$env:DB_URL = "jdbc:postgresql://127.0.0.1:5433/flow"
$env:DB_USERNAME = "postgres"
$env:DB_PASSWORD = "change-me"
$env:JWT_SECRET = "FLOW-LOCAL-DEV-JWT-SECRET-CHANGE-ME-32-BYTES"
$env:FLOW_BOOTSTRAP_ADMIN_USERNAME = "admin"
$env:FLOW_BOOTSTRAP_ADMIN_PASSWORD = "TestPassword-ChangeMe-123!"
$env:FLOW_POSTGRES_PORT = "5433"

Write-Host "[1/4] Starting PostgreSQL..."
Push-Location $ProjectRoot
try { docker compose up -d postgres } finally { Pop-Location }
if ($LASTEXITCODE -ne 0) { throw "Docker Compose gagal." }
Write-Host "PostgreSQL started on localhost:5433."
Write-Host ""

Write-Host "[2/4] Starting Spring Boot backend..."
$BackendCommand = 'Set-Location "' + $BackendDir + '"; .\gradlew.bat bootRun --no-daemon'
Start-Process powershell.exe -ArgumentList "-NoExit", "-Command", $BackendCommand
Write-Host "Backend process started."
Write-Host ""

Write-Host "[3/4] Starting web server..."
$WebCommand = 'Set-Location "' + $WebDir + '"; python -m http.server 5500'
Start-Process powershell.exe -ArgumentList "-NoExit", "-Command", $WebCommand
Write-Host "Web server process started."
Write-Host ""

Write-Host "[4/4] Waiting for backend..."
$MaxAttempts = 60
$Attempt = 0
while ($Attempt -lt $MaxAttempts) {
    Start-Sleep -Seconds 1
    $Attempt++
    try {
        $Response = Invoke-WebRequest -Uri "http://localhost:8080/api/health" -UseBasicParsing -TimeoutSec 2
        if ($Response.StatusCode -eq 200) {
            Write-Host ""
            Write-Host "========================================"
            Write-Host " FLOW READY"
            Write-Host "========================================"
            Write-Host ""
            Write-Host "Backend : http://localhost:8080"
            Write-Host "Web     : http://localhost:5500"
            Write-Host "Postgres: localhost:5433"
            Write-Host ""
            Write-Host "Login awal: admin / TestPassword-ChangeMe-123!"
            Write-Host ""
            Write-Host "Sekarang tinggal Run Android dari Android Studio."
            Write-Host ""
            exit 0
        }
    } catch { Write-Host -NoNewline "." }
}
Write-Host ""
throw "Backend tidak berhasil ready."
