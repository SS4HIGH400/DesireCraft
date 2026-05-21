chcp 65001 >nul
java -Xms2G -Xmx4G -XX:+UseG1GC -XX:+UnlockExperimentalVMOptions -XX:MaxGCPauseMillis=100 -XX:+DisableExplicitGC -XX:TargetSurvivorRatio=90 -XX:G1NewSizePercent=50 -XX:G1MaxNewSizePercent=80 -XX:InitiatingHeapOccupancyPercent=10 -XX:G1ReservePercent=15 -XX:SurvivorRatio=32 -Dusing.aikars.flags=https://emc.gs -Daikars.new.flags=true -jar server.jar nogui
pause