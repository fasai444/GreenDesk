@echo off
setlocal enabledelayedexpansion

REM Demo script for Plant Placement Optimizer (Windows)
echo ========================================
echo GreenDesk - Plant Placement Optimizer Demo
echo ========================================
echo.

set BASE=http://localhost:8080
set API=%BASE%/api

REM Step 1: Create Species
echo Step 1: Creating species...

curl -s -X POST "%API%/species" ^
  -H "Content-Type: application/json" ^
  -d "{\"name\":\"Tomato\",\"optimalWaterNeeds\":200,\"optimalTemperature\":22,\"optimalHumidity\":60,\"optimalLuxNeeds\":1500,\"baseGrowthRate\":1.5,\"seedProductionRate\":0.4}" > temp_tomato.json

for /f "tokens=2 delims=:," %%a in ('type temp_tomato.json ^| findstr "\"id\""') do (
    set TOMATO_ID=%%~a
    goto :tomato_done
)
:tomato_done
set TOMATO_ID=%TOMATO_ID:"=%
set TOMATO_ID=%TOMATO_ID: =%
echo [OK] Tomato species created: %TOMATO_ID%

curl -s -X POST "%API%/species" ^
  -H "Content-Type: application/json" ^
  -d "{\"name\":\"Basil\",\"optimalWaterNeeds\":150,\"optimalTemperature\":20,\"optimalHumidity\":55,\"optimalLuxNeeds\":1200,\"baseGrowthRate\":1.2,\"seedProductionRate\":0.3}" > temp_basil.json

for /f "tokens=2 delims=:," %%a in ('type temp_basil.json ^| findstr "\"id\""') do (
    set BASIL_ID=%%~a
    goto :basil_done
)
:basil_done
set BASIL_ID=%BASIL_ID:"=%
set BASIL_ID=%BASIL_ID: =%
echo [OK] Basil species created: %BASIL_ID%

curl -s -X POST "%API%/species" ^
  -H "Content-Type: application/json" ^
  -d "{\"name\":\"Carrot\",\"optimalWaterNeeds\":180,\"optimalTemperature\":18,\"optimalHumidity\":50,\"optimalLuxNeeds\":1300,\"baseGrowthRate\":1.0,\"seedProductionRate\":0.5}" > temp_carrot.json

for /f "tokens=2 delims=:," %%a in ('type temp_carrot.json ^| findstr "\"id\""') do (
    set CARROT_ID=%%~a
    goto :carrot_done
)
:carrot_done
set CARROT_ID=%CARROT_ID:"=%
set CARROT_ID=%CARROT_ID: =%
echo [OK] Carrot species created: %CARROT_ID%
echo.

REM Step 2: Create Forest
echo Step 2: Creating forest...

curl -s -X POST "%API%/forests" ^
  -H "Content-Type: application/json" ^
  -d "{\"name\":\"Optimization Demo Forest\",\"width\":12,\"height\":12}" > temp_forest.json

for /f "tokens=2 delims=:," %%a in ('type temp_forest.json ^| findstr "\"id\""') do (
    set FOREST_ID=%%~a
    goto :forest_done
)
:forest_done
set FOREST_ID=%FOREST_ID:"=%
set FOREST_ID=%FOREST_ID: =%
echo [OK] Forest created: %FOREST_ID% (12x12)
echo.

REM Step 3: Create Plants
echo Step 3: Creating plants...

set PLANT_IDS=
set COUNT=0

REM Create Tomatoes
for /L %%i in (1,1,4) do (
    curl -s -X POST "%BASE%/plants/create?name=Tomato%%i&speciesId=%TOMATO_ID%" > temp_plant.json
    for /f "tokens=2 delims=:," %%a in ('type temp_plant.json ^| findstr "\"id\""') do (
        set PID=%%~a
        set PID=!PID:"=!
        set PID=!PID: =!
        if !COUNT! GTR 0 set PLANT_IDS=!PLANT_IDS!,
        set PLANT_IDS=!PLANT_IDS!"!PID!"
        set /a COUNT+=1
        echo [OK] Created Tomato%%i: !PID!
    )
)

REM Create Basils
for /L %%i in (1,1,4) do (
    curl -s -X POST "%BASE%/plants/create?name=Basil%%i&speciesId=%BASIL_ID%" > temp_plant.json
    for /f "tokens=2 delims=:," %%a in ('type temp_plant.json ^| findstr "\"id\""') do (
        set PID=%%~a
        set PID=!PID:"=!
        set PID=!PID: =!
        if !COUNT! GTR 0 set PLANT_IDS=!PLANT_IDS!,
        set PLANT_IDS=!PLANT_IDS!"!PID!"
        set /a COUNT+=1
        echo [OK] Created Basil%%i: !PID!
    )
)

REM Create Carrots
for /L %%i in (1,1,3) do (
    curl -s -X POST "%BASE%/plants/create?name=Carrot%%i&speciesId=%CARROT_ID%" > temp_plant.json
    for /f "tokens=2 delims=:," %%a in ('type temp_plant.json ^| findstr "\"id\""') do (
        set PID=%%~a
        set PID=!PID:"=!
        set PID=!PID: =!
        if !COUNT! GTR 0 set PLANT_IDS=!PLANT_IDS!,
        set PLANT_IDS=!PLANT_IDS!"!PID!"
        set /a COUNT+=1
        echo [OK] Created Carrot%%i: !PID!
    )
)

echo.
echo Total plants created: %COUNT%
echo.

REM Step 4: Run Optimization
echo Step 4: Running genetic algorithm optimization...
echo (This may take 10-15 seconds)

curl -s -X POST "%API%/placement/optimize" ^
  -H "Content-Type: application/json" ^
  -d "{\"plantIds\":[%PLANT_IDS%],\"forestWidth\":12,\"forestHeight\":12}" > temp_optimize.json

echo [OK] Optimization complete!
type temp_optimize.json
echo.
echo.

REM Step 5: Apply Optimization
echo Step 5: Applying optimization to forest...

curl -s -X POST "%API%/placement/optimize-and-apply/%FOREST_ID%" ^
  -H "Content-Type: application/json" ^
  -d "[%PLANT_IDS%]" > temp_apply.json

echo [OK] Placement applied to forest
type temp_apply.json
echo.
echo.

REM Step 6: Generate Heatmap
echo Step 6: Generating heatmap for Tomato placement...

curl -s "%API%/placement/heatmap/%FOREST_ID%?species=Tomato" > temp_heatmap.json

echo [OK] Heatmap generated
echo.

REM Step 7: Get Suggestion
echo Step 7: Getting optimal position suggestion for Basil...

curl -s "%API%/placement/suggest/%FOREST_ID%?species=Basil" > temp_suggest.json

echo [OK] Suggestion:
type temp_suggest.json
echo.
echo.

REM Summary
echo ========================================
echo Demo Complete!
echo ========================================
echo.
echo Next steps:
echo 1. Open browser: %BASE%/placement-optimizer.html
echo 2. Select forest: Optimization Demo Forest
echo 3. View the optimized placement
echo 4. Try generating heatmaps for different species
echo.
echo Created resources:
echo   - Forest ID: %FOREST_ID%
echo   - Tomato Species ID: %TOMATO_ID%
echo   - Basil Species ID: %BASIL_ID%
echo   - Carrot Species ID: %CARROT_ID%
echo   - %COUNT% plants
echo.

REM Cleanup temp files
del temp_*.json 2>nul

pause
