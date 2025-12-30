@echo off
echo DeliveryCore v1.1.0 Basit Build
echo.

REM Target klasörünü temizle
if exist target rmdir /s /q target
mkdir target\classes

REM Resource dosyalarını kopyala
xcopy /s /y src\main\resources\* target\classes\

REM JAR oluştur
jar cf target\DeliveryCore-v1.1.0-resources.jar -C target\classes .

echo.
echo Basit JAR oluşturuldu: target\DeliveryCore-v1.1.0-resources.jar
echo Bu sadece konfigürasyon dosyalarını içerir.
echo.
pause