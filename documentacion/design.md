# Manual del Cliente Android

## Introducción

Este proyecto consiste de un servidor Web HTTP accesible a través de una REST API para la aplicación **FIUBER**. **FIUBER** es una aplicación intencionada para conectar a los pasajeros con los conductores de vehículos que ofrecen servicio de transporte particular. Esta aplicación permite a los potenciales pasajeros: obtener estimaciones de costo un viaje antes de realizarlo, elegir al conductor que desean, solicitar al chofer y una vez que terminan el viaje realizar el pago utilizando cualquiera de los medios de pago disponibles. 

Este sistema se basa en un diseño de 3 capas que permite el funcionamiento de la aplicación:

+ **Cliente Android**
+ [Application Server](https://github.com/fi-ubers/app-server)
+ [Shared Server](https://github.com/fi-ubers/shared-server)

Este proyecto provee una implementación para la capa de Cliente Android del sistema. 

## Relación con el Application y Shared Servers

Para implementar esta aplicación se utilizó una arquitectura de 3 capas (3-Tier), donde el *App Server* representa la capa lógica o de negocios. La capa de datos es provista por el [Shared Server](https://github.com/fi-ubers/shared-server) y es allí donde se almacenan los datos de los usuarios de la aplicación, tanto conductores como pasajeros, los viajes y los servidores activos. La aplicación está pensada para permitir la coexistencia de múltiples *App Servers* que utilizan al *Shared Server* como servicio web para almacenar datos y como punto de acceso a la API de pagos, que se provee de forma externa.

Diagrama general de capas:

![](https://github.com/fi-ubers/app-server/blob/master/docs/ArchDiagram.png)


## Arquitectura y diseño

La arquitectura de la aplicación está organizada en clases Activity de Android, según como se muestra en el siguiente esquema. Debido a que los usuarios de la aplicación pueden ser tanto *pasajeros* como *conductores*, se especifican estas diferencias en el diagrama con colores, señalando aquellas activities sólo accesibles por pasajeros con azul, y esas sólo accesibles por conductores en rojo.

![](https://github.com/fi-ubers/client/blob/master/documentacion/activities.png)


Las funciones de las activities señaladas en el diagrama son:

- LoginActivity: Es el punto de entrada al sistema. Los usuarios pueden loggearse con Facebook, o acceder a la pantalla de registro manual.
- ManuaSignInActivity: Permite al usuario crear una nueva cuenta FIUBER o ingresar a la aplicación con una cuenta FIUBER existente.
- MainActivity: La Activity truncal de la aplicación. Presenta un menú con notificaciones para que los usuarios puedan acceder a las demás activities.
- ProfileActivity: Brinda la posibilidad de ver y modificar los datos de la cuenta de usuario.
- CarsActivity: Permite al conductor registrar o dar de baja sus autos en el sistema.
- SelectTripActivity: Activity con un mapa. Muestra la ubicación del usuario, y la de otros usuarios cercanos. Su función varía dependiendo el tipo del usuario: a los pasajeros, les permite buscar lugares y generar nuevos viajes; a los conductores, les deja buscar viajes para aceptar.
- TripInfoActivity: Una vez que el pasajero diagrama un viaje, esta pantalla le hace un resumen del mismo, estima su costo y le permite confirmarlo.
- ChoosePassengerActivity: Lista para el conductor los viajes cercanos disponibles, y le permite verlos en el mapa para aceptarlos.
- ChatActivity: Pantalla de chat para poder conversar con el otro usuario vinculado en el viaje.
- TripOtherInfoActivity: Muestra un resumen del viaje y del otro usuario vinculado a este.
- TripEnRouteActivity: Permite ver el progreso del viaje en curso (mientras se está viajando) y terminarlo.
- PayingActivity: Permite al pasajero pagar su viaje luego de finalizarlo.


### Estados de pasajeros y conductores

Al ser la aplicación cliente una por roles, fue necesario distinguir los estados de pasajeros y conductores, identificando las acciones que cada uno podía hacer en cada situación.

Así, los estados de un pasajero son los siguientes:

* *Idle*: el pasajero está recien logueado en la aplicación, puede ver conductores cercanos, averiguar por el costo de un viaje o bien proponer un viaje nuevo y esperar por conductores.
* *Waiting Confirmation*: un pasajero que acaba de solicitar un viaje está en este estado, y representa la espera hasta que un conductor decida tomarlo. El pasajero puede, en cualquier momento, cambiar de parecer y cancelar el viaje, regresando al estado *idle*.
* *Examining Driver*: cuando un conductor acepta el viaje propuesto por el pasajero, éste último entrará en este estado. En este momento, él podrá ver la información del conductor y decidir entre confirmar el viaje o rechazarlo.
* *Waiting Driver*: el pasajero que haya aceptado a un conductor estará en este estado, e indica que está esperando a que lo pasen a buscar. Durante este estado se puede todavía cancelar el viaje (como en *waiting confirmation*), o bien iniciarlo cuando el conductor llegue al lugar.
* *Travelling*: cuando el viaje inició tanto pasajero como conductor entrarán en este estado, que representa el viaje en sí. Se trackearán las posiciones de ambos usuarios para definir el camino real del viaje. La única acción permitida en este estado es la de finalizar el viaje.
* *Arrived*: este es el estado final del pasajero en un viaje. Representa que el viaje terminó y que debe efectuar el pago del mismo. En este momento se permite realizar una reseña del conductor. El ciclo de vida del viaje termina cuando el pasajero efectúa el pago, volviendo él al estado *idle*.

En cambio, los estados de un conductor son:

* *On Duty*: un conductor en este estado no está asociado a ningún viaje. Puede ver otros usuarios cercanos, y obtener una lista de viajes propuestos que requieren de un conductor. Viendo la lista de viajes puede elegir tomar uno.
* *Waiting Confirmation*: un conductor que decida aceptar un viaje todavía no estará asociado al mismo, pues debe esperar a que el pasajero que lo propuso le dé su aprobación. En este estado, puede ocurrir que tanto el pasajero lo rechace (en cuyo caso, el conductor volverá al estado *on duty*) como que lo acepte.
* *Going To Pickup*: cuando el conductor es confirmado, debe ir al punto de encuentro establecido por el viaje. Una vez ahí, deberá comenzar el viaje.
* *Travelling*: cuando el viaje inició tanto pasajero como conductor entrarán en este estado, que representa el viaje en sí. Ver el estado homónimo en los pasajeros.

Toda la información de los usuarios y la lógica de sus estados (y, por ende, de las acciones que pueden realizar en cada caso) fue encapsulada en las clases UserInfo y UserStatus.

### Code snippets

A continuación se muestran unos pequeños fragmentos de código que ejemplifican las principales características de la arquitectura y diseño de la aplicación:

Inicio de sesión (log in) de un usuario:
```java
public void logUser() {
    try {
        Jsonator jnator = new Jsonator();
        String toSendJson = jnator.writeUserLoginCredentials(mUserId, mPassword, " ");
        ConexionRest conn = new ConexionRest(this);
        String urlReq = conn.getBaseUrl() + "/users/login";
        Log.d("LoginActivity", "JSON to send: "+ toSendJson);
        conn.generatePost(toSendJson, urlReq, null);
    }
    catch(Exception e){
        Log.e("LoginActivity", "Manual log in error: ", e);
    }
}
```

Escritura de un JSON (método de pago) para enviar al Application Server usando Jsonator
```java
public String writePaymentAction(PaymethodInfo pm){
	JSONObject objJson = new JSONObject();
	JSONObject innerPayment = new JSONObject();
	JSONObject payParameters = new JSONObject();

	try {
	    innerPayment.put("paymethod", pm.method);
	    payParameters.put("ccvv", pm.cardCcvv);
	    payParameters.put("expiration_month", pm.expMonth);
	    payParameters.put("expiration_year", pm.expYear);
	    payParameters.put("number", pm.cardNumber);
	    payParameters.put("type", pm.cardType);
	    innerPayment.put("parameters", payParameters);
	    objJson.put("paymethod", innerPayment);
	    objJson.put("action", "pay");
	}
	catch (Exception e) {
	    Log.e("Fiuber Jsonator", "exception", e);
	}

	return objJson.toString();
	}
```
Crear lista de viajes para aceptar (chofer), luego de recibirlos del Application Server en formato JSON:
```java
private void getTripsList(String servResponse){
	int itemChecked = listView.getCheckedItemPosition();
	listView.clearChoices();
	Log.d("ChoosePassengerActivity", "GET trips response:" + servResponse);
	Jsonator jnator = new Jsonator();
	trips = jnator.readTripsProposed(servResponse);
	ArrayAdapter<String> mAdapter = (ArrayAdapter<String>) listView.getAdapter();
	mAdapter.clear();
	Iterator<ProtoTrip> it = trips.iterator();
	while(it.hasNext()){
		ProtoTrip nexTrip = it.next();
		String addrO = nexTrip.getOriginName().split(",")[0];
		String addrD = nexTrip.getDestinationName().split(",")[0];
		String fromTo =  "From: " + addrO + "\nTo: " + addrD;
		mAdapter.add(fromTo);
	}
	listView.setItemChecked(itemChecked, true);
	return;
}
```

Usar Firebase para enviar un mensaje a otro usuario al apretar botón de enviar:
```java
public void onClick(View view) {
	if(input.getText().toString().trim().equals(""))
		return; // empty string, does nothing

	DatabaseReference dr = FirebaseDatabase.getInstance().getReference(chatName);
	String msg = input.getText().toString();
	String uId = UserInfo.getInstance().getUserId();
	String uName = UserInfo.getInstance().getFirstName();
	// Post to firebase db
	ChatMessage chatMsg = new ChatMessage(msg, uName, uId);
	dr.push().setValue(chatMsg);
	input.setText("");
}
```


## Dependencias y herramientas

La aplicación de cliente Android depende de las siguientes librerías externas:


- **Google Maps API**

  Se usó Google Maps API como soporte para poder visualizar y manejar mapas en toda la aplicación, así como para la traducción de coordenadas geográficas a lugares relevantes y viceversa (servicios de geolocalización directa e inversa). A partir de la información provista por Google Maps, los usuarios pueden confeccionar viajes, ver a los demás usuarios cercanos, detectar viajes por cercanía y demás.

- **Firebase CM**

  Se utilizó Firebase como servicio web para poder proveer un servicio de mensajería (chat) entre el pasajero y el conductor. Adicionalmente se utilizó Firebase para recibir notificaciones push de eventos (como mensajes nuevos o la cancelación de un viaje).

- **Facebook SDK**

  Para facilitar el inicio de sesión en la aplicación para el usuario, se integró a la aplicación con Facebook SDK. De esa manera, los usuarios pueden ahorrarse el tener que cargar todos sus datos personales, ya que el cliente Android los obtiene automáticamente al vincularse con la cuenta de Facebook del usuario.

## Bugs conocidos y puntos a mejorar

Debido a restricciones temporales, existen algunos aspectos de este proyecto que requieren ser mejorados o concluidos. A continuación, realizamos una breve descripción de las falencias y bugs detectados hasta el momento:

+ **Inicio y fin de viaje sin doble confirmación:** Actualmente, tanto chofer como pasajero deben ponerse de acuerdo a la hora de iniciar o terminar el viaje, ya que ambos deben dar su consentimiento presionando un botón. La razón para hacerlo de esta manera fue tratar de evitar que, al tener sólo uno de los dos usuarios la potestad de iniciar o terminar el viaje, éste pudiera hacerlo en un momento no correcto, perjudicando al otro usuario. No obstante, se reconoció que esta doble confirmación, aunque más justa, vuelve mucho más molesta la aplicación, por lo que un cambio sería bastante deseable.

+ **Notificaciones configurables:** Las notificaciones push o con mensajes dentro de la aplicación pueden resultar molestas para el usuario, en especial si ya estaba esperando que esos eventos ocurrieran. Una característica buena para agregar sería la de permitir al usuario elegir qué tipo de notificaciones recibir, e incluso desactivar las notificaciones por completo.

+ **Botón de denuncia (pánico) en la aplicación:** Creemos que un simple botón que permita denunciar al otro usuario durante un viaje de forma inmediata sería de gran valor para la aplicación, pues ayudaría a los usuarios a viajar más tranquilos y seguros.





