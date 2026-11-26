# B1---MiniProyecto
# Programación Funcional y Reactiva

## Mini Proyecto: Higher Order Functions en Criptografía

## Objetivos

- Aplicar los conocimientos sobre funciones de orden superior (*Higher Order Functions*).
- Utilizar funciones como parámetros para definir estrategias de cifrado dinámicas.
- Implementar operaciones de transformación sobre colecciones inmutables (`map`).
- Implementar un manejo adecuado de excepciones y validación de datos.

## Descripción del Trabajo

El objetivo es crear un motor de cifrado que reciba una función de transformación como parámetro. Se deben implementar dos métodos de cifrado específicos:

### 1. Cifrado César (Desplazamiento)

Consiste en mover cada letra del alfabeto un número fijo de posiciones.

```
E(x, k) = (pos(x) + k) mod 26
```

Donde `x` es el carácter y `k` es la clave (entero).

### 2. Cifrado XOR (Lógico)

Aplica la operación *Exclusive OR* a nivel de bits entre el código ASCII del carácter y una clave numérica.

```
E(x, k) = ASCII(x) ⊕ k
```

> **Nota de implementación:** para realizar esta operación a nivel de bits en el código, se debe utilizar el operador `^` (caret). Este método tiene la propiedad de que aplicar la misma operación dos veces devuelve el valor original.

También se debe implementar un manejo adecuado de excepciones, tales como:

- Cadenas de texto vacías o nulas.
- Claves inválidas.
- Caracteres no soportados.

## Actividades

### 1. Implementación de Funciones

Crear una función de orden superior llamada `procesar_mensaje` (o similar) con los siguientes parámetros:

- Mensaje (`String`).
- Clave (`Int`).
- Función de cifrado (`(Char, Int) => Char`).

La función debe aplicar la transformación a cada carácter del mensaje utilizando `map` e incluir bloques de manejo de excepciones (`Try/Catch` o `Try/Success/Failure`).
```scala
import scala.util.{Try, Success, Failure}
def procesar_mensaje(mensaje: String, clave: Int, cifrado: (Char, Int) => Char): Try[String] = {
  if (mensaje == null || mensaje.isEmpty) {
    Failure(new IllegalArgumentException(
      "El mesaje es nulo o esta vacio"))
  } else{
    Success(mensaje.map(c => cifrado (c, clave)))
  }
}
```

### 2. Pruebas de Cifrado y Descifrado

Utilizar las funciones para resolver los siguientes casos:

#### 1. Caso César

- Cifrar la palabra `"FUNCIONAL"` con clave `k = 3`.
- Descifrar el resultado obtenido (usando clave `-3` o lógica inversa) para recuperar el mensaje original.

#### 2. Caso XOR

- Cifrar la palabra `"scala"` con clave `k = 5`.
- Utilizar el operador `^` dentro de la función lambda para transformar el carácter.
- Volver a aplicar la función con el resultado y la misma clave `k = 5` para demostrar que se recupera `"scala"`.

### 3. Manejo de Errores

Implementar pruebas donde se fuerce el fallo para demostrar la robustez del código (ej. pasar una clave excesivamente grande o caracteres especiales no manejados) y capturar el error elegantemente.

## Ejemplos de Ejercicio

A continuación se detalla el paso a paso lógico que el código debe realizar internamente.
