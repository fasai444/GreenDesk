param(
    [string]$BaseUrl = "http://localhost:8080"
)

$ErrorActionPreference = "Stop"
$Failures = New-Object System.Collections.Generic.List[string]
$Session = New-Object Microsoft.PowerShell.Commands.WebRequestSession

function Pass($Message) {
    Write-Host "[OK] $Message" -ForegroundColor Green
}

function Fail($Message) {
    $Failures.Add($Message) | Out-Null
    Write-Host "[FAIL] $Message" -ForegroundColor Red
}

function Assert($Condition, $Message) {
    if ($Condition) { Pass $Message } else { Fail $Message }
}

function Get-Json($Path) {
    Invoke-RestMethod -Method Get -Uri "$BaseUrl$Path" -WebSession $Session
}

function Post-Json($Path, $Body) {
    Invoke-RestMethod -Method Post -Uri "$BaseUrl$Path" -WebSession $Session -ContentType "application/json" -Body ($Body | ConvertTo-Json -Depth 20)
}

function Put-Query($Path) {
    Invoke-RestMethod -Method Put -Uri "$BaseUrl$Path" -WebSession $Session
}

Write-Host "GreenDesk smoke tests: $BaseUrl" -ForegroundColor Cyan

try {
    $homeResponse = Invoke-WebRequest -UseBasicParsing -Uri "$BaseUrl/home.html"
    Assert ($homeResponse.StatusCode -eq 200) "Frontend home.html accessible"

    foreach ($page in @("dashboard.html", "forest-compare.html", "recommendations.html", "whatif.html")) {
        $res = Invoke-WebRequest -UseBasicParsing -Uri "$BaseUrl/$page"
        Assert ($res.StatusCode -eq 200) "Frontend $page accessible"
    }

    $login = Post-Json "/api/auth/login" @{ username = "demo"; password = "demo123" }
    Assert ($login.username -eq "demo") "Authentification demo reussie pour les APIs securisees"

    foreach ($path in @("/api/species", "/api/plants", "/api/forests", "/api/effects", "/api/seasons")) {
        $null = Get-Json $path
        Pass "Regression API $path accessible"
    }

    $stamp = Get-Date -Format "yyyyMMddHHmmss"
    $speciesName = "SmokeSpecies_$stamp"

    $species = Post-Json "/api/species" @{
        name = $speciesName
        optimalWaterNeeds = 100
        optimalTemperature = 22
        optimalHumidity = 60
        optimalLuxNeeds = 3000
        baseGrowthRate = 0.5
        seedProductionRate = 0.2
    }
    Assert ($species.id) "Species test creee"

    $forestA = Post-Json "/api/forests" @{ name = "SmokeForestA_$stamp"; width = 5; height = 5 }
    $forestB = Post-Json "/api/forests" @{ name = "SmokeForestB_$stamp"; width = 5; height = 5 }
    Assert ($forestA.id -and $forestB.id) "Deux forets test creees"

    $speciesId = [uri]::EscapeDataString($species.id)
    $plantHealthy = Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/plants/create?name=SmokeHealthy_$stamp&speciesId=$speciesId&water=100&temperature=22&humidity=60&lux=3000" -WebSession $Session
    $plantRisky = Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/plants/create?name=SmokeRisky_$stamp&speciesId=$speciesId&water=25&temperature=36&humidity=25&lux=600" -WebSession $Session
    $plantOther = Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/plants/create?name=SmokeOther_$stamp&speciesId=$speciesId&water=100&temperature=22&humidity=60&lux=3000" -WebSession $Session
    Assert ($plantHealthy.id -and $plantRisky.id -and $plantOther.id) "Trois plantes test creees"

    $null = Post-Json "/api/forests/$($forestA.id)/plants" @{ plantId = $plantHealthy.id; x = 0; y = 0 }
    $null = Post-Json "/api/forests/$($forestA.id)/plants" @{ plantId = $plantRisky.id; x = 1; y = 0 }
    $null = Post-Json "/api/forests/$($forestB.id)/plants" @{ plantId = $plantOther.id; x = 0; y = 0 }
    Pass "Plantes ajoutees aux forets"

    $compare = Get-Json "/api/forests/compare?forestId1=$($forestA.id)&forestId2=$($forestB.id)"
    Assert ($compare.forest1.totalPlants -eq 2 -and $compare.forest2.totalPlants -eq 1 -and $compare.winner) "Feature 5: comparaison de forets liee au backend"

    $recsPlant = Get-Json "/api/recommendations/plant/$($plantRisky.id)"
    Assert (($recsPlant | Measure-Object).Count -gt 0) "Feature 6: recommandations par plante retournees"
    Assert (($recsPlant | Where-Object { $_.type -in @("WATERING", "COOLING", "HUMIDITY", "LIGHTING") } | Measure-Object).Count -gt 0) "Feature 6: recommandations coherentes avec plante en risque"

    $recsForest = Get-Json "/api/recommendations/forest/$($forestA.id)"
    Assert (($recsForest | Measure-Object).Count -gt 0) "Feature 6: recommandations par foret retournees"

    $scenarios = Get-Json "/api/whatif/scenarios"
    Assert (($scenarios | Where-Object { $_.id -eq "CANICULE" } | Measure-Object).Count -eq 1) "What-If: scenarios predefinis retournes"

    $before = Get-Json "/api/plants/$($plantRisky.id)"
    $sim = Post-Json "/api/whatif/simulate" @{
        forestId = $forestA.id
        tempDelta = 12
        waterDelta = -20
        humidityDelta = -15
        luxDelta = 1000
        ticks = 2
    }
    Assert ($sim.summary.totalPlants -eq 2 -and $sim.plants.Count -eq 2) "What-If: simulation retournee pour les plantes de la foret"

    $after = Get-Json "/api/plants/$($plantRisky.id)"
    Assert ([math]::Abs($before.stressIndex - $after.stressIndex) -lt 0.0001) "What-If: simulation non destructive pour la BDD"

    $invalidSimFailed = $false
    try {
        $null = Post-Json "/api/whatif/simulate" @{ forestId = $forestA.id; ticks = 99 }
    } catch {
        $invalidSimFailed = $true
    }
    Assert $invalidSimFailed "What-If: validation backend sur ticks invalide"

    $updated = Put-Query "/api/plants/$($plantHealthy.id)?water=80&temperature=24&humidity=55&lux=2500"
    Assert ($updated.id -eq $plantHealthy.id) "Regression: modification plante fonctionne encore"

    $plantsInForest = Get-Json "/api/forests/$($forestA.id)/plants"
    Assert (($plantsInForest | Measure-Object).Count -eq 2) "Regression: lecture plantes d'une foret fonctionne encore"

} catch {
    Fail $_.Exception.Message
}

if ($Failures.Count -gt 0) {
    Write-Host ""
    Write-Host "$($Failures.Count) test(s) en echec:" -ForegroundColor Red
    $Failures | ForEach-Object { Write-Host "- $_" -ForegroundColor Red }
    exit 1
}

Write-Host ""
Write-Host "Tous les smoke tests sont passes." -ForegroundColor Green
