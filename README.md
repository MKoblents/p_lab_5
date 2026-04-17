Для запуска программы вам необходимо:
1. Скачать весь репозиторий.
2. Скачать, если нету, пакет maven.
   Команда для убунты: `sudo apt insatall maven`
3. Скомпилировать проект: `mvn clean package`
4. Запустить в отдельном терминале сервер: `java -jar target/p_lab_5-server.jar`
5. Скачать tMUX: `sudo apt install tmux`
6. Запустить сессию tMUX: `tmux`
7. Запустить в ней клиента: `java -jar target/p_lab_5-client.jar`