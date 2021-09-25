
echo remove application.properties from jar file
PowerShell -NoProfile -ExecutionPolicy Bypass -Command "& rm target/*.jar"
PowerShell -NoProfile -ExecutionPolicy Bypass -Command "& mv target/greenpole-user-management-0.0.1.jar.original target/greenpole-user-management-0.0.1.jar"
