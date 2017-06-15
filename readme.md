Track & TrainMe
===============

Introducci�n
------------

Track & TrainMe es una aplicaci�n desarrollada en Java cuyo objetivo es
el de reconocer actividades. Dispone de un clasificador con cuatro
actividades por defecto (caminar, barrer, apluadir y estar quieto).
A su vez, podemos llevar un historial de las actividades que hemos realizado.

Por otra parte, la aplicaci�n permite a�adir nuevas actividades personalizadas
al usuario.

Instalaci�n
-----------

Para poder utilizar la aplicaci�n necesitaremos un tel�fono m�vil cuyo sistema
operativo sea Android (versi�n m�nima 5.1 Lollipop). Para poder instalar la
aplicaci�n bastar� con instalar el .apk de la aplicaci�n.

Organizaci�n del proyecto
-------------------------

El proyecto se divide en tres grandes carpetas: la carpeta apk donde alamacenamos
la �ltima versi�n de la aplicaci�n, la carpeta app la cual se organiza igual que
cualquier otra aplicaci�n Android, y la carpeta Servidor, que se encarga de recibir
peticiones y devuelve clasificadores personalizados. Los ficheros de esta �ltima 
carpeta est�n implementados en Python.

Dentro de la carpeta app hemos organizado los paquetes de la siguiente manera:
	+ Las clases Activity se encuentran en el paquete Activities.
	+ Las clases encargadas del uso de la base de datos se encuentran en el
	paquete DataBase
	+ Las clases que se encargan de llevar a cabo la pol�tica de clasificaci�n
	se encuentran en el paquete EstadosFSM.
	+ Las clases encargadas de las vistas de la ventana principal se encuentran 
	en el paquete Fragments.
	+ Las clases que se encargan del manejo del historial de actividades se
	encuentra en el paquete Historial.
	+ Los distintos Services de la aplicaci�n se encuentran en el paquete
	Service.
	+ Las clases que se encargan de la implementaci�n de los diferentes Threads
	se encuentran en el paquete Threads.
	+ En el paquete Utilidades podemos encontrar m�todos gen�ricos que no tienen
	una ubicaci�n espec�fica en el proyecto.