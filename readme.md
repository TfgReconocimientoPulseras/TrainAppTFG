Track & TrainMe
===============

Introducción
------------

Track & TrainMe es una aplicación desarrollada en Java cuyo objetivo es
el de reconocer actividades. Dispone de un clasificador con cuatro
actividades por defecto (caminar, barrer, apluadir y estar quieto).
A su vez, podemos llevar un historial de las actividades que hemos realizado.

Por otra parte, la aplicación permite añadir nuevas actividades personalizadas
al usuario.

Instalación
-----------

Para poder utilizar la aplicación necesitaremos un teléfono móvil cuyo sistema
operativo sea Android (versión mínima 5.1 Lollipop). Para poder instalar la
aplicación bastará con instalar el .apk de la aplicación.

Organización del proyecto
-------------------------

El proyecto se divide en tres grandes carpetas: la carpeta apk donde alamacenamos
la última versión de la aplicación, la carpeta app la cual se organiza igual que
cualquier otra aplicación Android, y la carpeta Servidor, que se encarga de recibir
peticiones y devuelve clasificadores personalizados. Los ficheros de esta última 
carpeta están implementados en Python.

Dentro de la carpeta app hemos organizado los paquetes de la siguiente manera:
	+ Las clases Activity se encuentran en el paquete Activities.
	+ Las clases encargadas del uso de la base de datos se encuentran en el
	paquete DataBase
	+ Las clases que se encargan de llevar a cabo la política de clasificación
	se encuentran en el paquete EstadosFSM.
	+ Las clases encargadas de las vistas de la ventana principal se encuentran 
	en el paquete Fragments.
	+ Las clases que se encargan del manejo del historial de actividades se
	encuentra en el paquete Historial.
	+ Los distintos Services de la aplicación se encuentran en el paquete
	Service.
	+ Las clases que se encargan de la implementación de los diferentes Threads
	se encuentran en el paquete Threads.
	+ En el paquete Utilidades podemos encontrar métodos genéricos que no tienen
	una ubicación específica en el proyecto.