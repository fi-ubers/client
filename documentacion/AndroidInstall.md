
# client

Aplicación de cliente Android que interactuará con el app-server de FiUber.

Por el momento, sólo es un _hello world_ en Android que además muestra la hora.

# Correr la apliación en el teléfono

Primero, deben haber instalado Android Studio [de su página](https://developer.android.com/studio/index.html) junto con todas las cosas de Java y JDK. Sigan los pasos de las primeras tres secciones del siguiente [tutorial](https://askubuntu.com/questions/634082/how-to-install-android-studio-on-ubuntu). *No sigan* con las siguientes, pues los llevará a instalar versiones del SDK y sus herramientas más nuevas de las que necesitamos. Para los que estén usando Android Studio en un OS de 64 bits, deberían también ver si tienen algunas [librerías de 32 bits](https://stackoverflow.com/questions/28314139/how-to-install-android-studio-on-ubuntu).

Si todo salió bien, se les abrirá el Android Studio en la pantalla "Welcome to Android Studio". Elijan _Import project_ y marquen la carpeta client del repo para que obtenga todo el código del proyecto. Luego de cargar un rato, se intentará buildear el proyecto y fallará por no tener las SDK tools necesarias. Sin embargo, abajo a la izquierda les irán apareciendo todas las dependencias que no tienen instaladas, y podrán hacer clic en los links en azul para instalarlas (Nota: si esto no es así, es posible que se haya instalado igual el último SDK. Ir al SDK manager arriba a la derecha, y en la solapa de SDK verificar que el que está siendo usado es el Kit Kat 4.4 API 19. Si no está ese tildado, destildar el que esté tildado y tildar ese para bajarlo. También puede ser que se les haya bajado el SDK platform tools 26 en vez del 23, en cuyo caso se puede solucionar con otros pasos de más abajo.). Continuar así hasta que hayan instalado todo y se pueda correr build correctamente.

Para poder enchufar el celular y correr la aplicación, deben tener habilitadas las [opciones de desarrollador](https://www.androidcentral.com/how-enable-developer-settings-android-42) en su celular, y tildada la opción de debug vía USB. Hecho esto, conecten el celular vía USB y confirmen los mensajes que puedan salirles. En Android Studio, en la ventana _Andorid Monitor_ (ventana 6, abajo a la izquierda) deberían poder ver su teléfono conectado, y ver un montón de mensajes con timestamps que se actualizan cada tanto.

Si todo esto fue bien, pueden darle _Build > Clean project_ y luego _Run_ para correr su aplicación en el teléfono. Deberán elegir su teléfono en una lista de dispositivos conectados, y darle OK. Desbloqueen su teléfono, y ¡listo! Tendrán Fiuber instalado en su teléfono y podrán correrlo sin problemas. Para poder desconectar su celular, simplemente cierren la aplicación del teléfono o denle a _Stop_ en el Android Studio.

# Problemas haciendo que todo funque

* Si les sale algún problema del Debug Bridge (o ADB): El [ADB](https://developer.android.com/studio/command-line/adb.html?hl=es-419) es lo que te deja conectar Android Studio con tu teléfono enchufado. Es fácil [bajarlo desde la terminal](https://www.youtube.com/watch?v=DV4_A_YSwO8). Probablemente aún después de reconocer el adb les siga tirando el mismo error, por lo que deberán seguir [estos otros pasos](https://stackoverflow.com/questions/34236449/adb-connection-error-unable-to-create-debug-bridge-unable-to-start-adb-server).
* Si les tira algún mensaje del estilo "No JDK found" o "Cannot run under Java 1.7": Instalaron Java 8, pero ya tenían de antes otra versión de Java instalada, que estaban usando por defecto. Tienen que cambiar las variables de entorno para que tome Java 8 como el por default. Nota: yo hice eso, pero me siguió tirando el error. Directamente lo que hice fue [eliminar todo rastro de java](https://askubuntu.com/questions/702274/error-while-setting-up-jdk8-on-ubuntu-14-04lts) y reinstalarlo.
* Si les tira un mensaje de error "Could not find method android() for arguments...": Tienen que remover unas [líneas del build.gradle](https://stackoverflow.com/questions/42475704/error16-0-could-not-find-method-android-for-arguments-on-root-project-del).
* Si les tira un mensaje de error "command /usr/local/android-studio/jre/bin/java finished with non-zero exit": Seguir los [siguientes pasos](https://stackoverflow.com/questions/41426033/errorprocess-command-usr-local-android-studio-jre-bin-java-finished-with-n).

