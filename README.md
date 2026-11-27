# Mini Proyecto: Higher Order Functions en Criptografía

**Programación Funcional y Reactiva** · Scala Worksheet · _Tu nombre aquí_

---

## Objetivos

- Aplicar los conocimientos sobre funciones de orden superior (*Higher Order Functions*).
- Utilizar funciones como parámetros para definir estrategias de cifrado dinámicas.
- Implementar operaciones de transformación sobre colecciones inmutables (`map`).
- Implementar un manejo adecuado de excepciones y validación de datos.

---

## 1. Implementación de funciones

Una **función de orden superior** es aquella que recibe otras funciones como parámetro o las devuelve. Aquí, el motor de cifrado `procesar_mensaje` recibe la función de transformación `(Char, Int) => Char`, lo que permite cambiar de algoritmo sin modificar el motor.

Para transformar el mensaje se usa **`map`**, que aplica una función a cada elemento de una colección inmutable y devuelve una colección nueva, sin bucles ni variables mutables.

Para el manejo de errores se usa **`Try`**, que representa una operación que puede fallar: si sale bien devuelve `Success(valor)`; si lanza una excepción, la captura y devuelve `Failure(error)`.

```scala
// 1. Implementacion de funciones
import scala.util.{Try, Success, Failure}
def procesar_mensaje(mensaje: String, clave: Int, cifrado: (Char, Int) => Char): Try[String] = {
  if (mensaje == null || mensaje.isEmpty) {
    Failure(new IllegalArgumentException(
      "El mesaje es nulo o esta vacio"))
  }  else if (clave < -1000 || clave > 1000) {
    Failure(new IllegalArgumentException(
      s"Clave invalida: $clave (rango permitido: -1000 a 1000)"))
  } else{
    Success(mensaje.map(c => cifrado (c, clave)))
  }
}
```

### Cifrado César

Desplaza cada letra `k` posiciones en el alfabeto:

```
E(x, k) = (pos(x) + k) mod 26
```

Para descifrar se usa la clave `-k`. Como el operador `%` devuelve valores negativos con números negativos, se usa `Math.floorMod`, que siempre da un resultado entre `0` y `25`.

```scala
// Desplazar letras
def desplazar(c: Char, base: Char, k: Int): Char =
  (base + Math.floorMod(c - base + k, 26)).toChar

// Cifrado cesar
def cifrado_cesar(c: Char, k: Int): Char = {
  if (c >= 'A' && c <= 'Z') desplazar(c, 'A', k)
  else if (c >= 'a' && c <= 'z') desplazar(c, 'a', k)
  else throw new IllegalArgumentException(s"Caracter no soportado: '$c'")
}
```

Ejemplo con `"AD"` y `k = 1`: `A (0) → (0+1) mod 26 = 1 → B` y `D (3) → (3+1) mod 26 = 4 → E`, resultado `"BE"`.

### Cifrado XOR

Aplica la operación *Exclusive OR* a nivel de bits entre el código ASCII del carácter y la clave, con el operador `^`:

```
E(x, k) = ASCII(x) ⊕ k
```

Como `(a ⊕ k) ⊕ k = a`, aplicar la misma operación dos veces con la misma clave devuelve el original.

```scala
//Cifrado XOR
def cifrado_xor(c: Char, k: Int): Char = {
  if (k < 0 || k > 127)
    throw new IllegalArgumentException(s"Clave XOR invalida: $k (debe estar entre 0 y 127)")
  if (c > 127)
    throw new IllegalArgumentException(s"Caracter no ASCII: '$c'")
  (c ^ k).toChar
}
```

---

## 2. Pruebas de cifrado y descifrado

### Caso César: `"FUNCIONAL"` con `k = 3`

```scala
// 2.1 Caso Cesar
val cesar_cifrado = procesar_mensaje("FUNCIONAL", 3, cifrado_cesar)
// Success(IXQFLRQDO)

// Descifrar con clave -3 (logica inversa)
val cesar_descifrado = cesar_cifrado.flatMap(t => procesar_mensaje(t, -3, cifrado_cesar))
// Success(FUNCIONAL)
```

| F | U | N | C | I | O | N | A | L |
|:-:|:-:|:-:|:-:|:-:|:-:|:-:|:-:|:-:|
| I | X | Q | F | L | R | Q | D | O |

### Caso XOR: `"scala"` con `k = 5`

Paso detallado para `'s'`: `115 ^ 5` → `0111 0011 ⊕ 0000 0101 = 0111 0110` = `118` = `'v'`.

```scala
// 2.2 Caso XOR (operador ^ dentro de la lambda)
val xor_cifrado = procesar_mensaje("scala", 5, (c, k) => (c ^ k).toChar)
// Success(vfdid)

// Misma funcion y misma clave para recuperar el original
val xor_descifrado = xor_cifrado.flatMap(t => procesar_mensaje(t, 5, (c, k) => (c ^ k).toChar))
// Success(scala)
```

| Carácter | ASCII | XOR 5 | Res. ASCII | Cifrado |
|:--------:|:-----:|:-----:|:----------:|:-------:|
| s | 115 | 115 ⊕ 5 | 118 | v |
| c | 99 | 99 ⊕ 5 | 102 | f |
| a | 97 | 97 ⊕ 5 | 100 | d |
| l | 108 | 108 ⊕ 5 | 105 | i |
| a | 97 | 97 ⊕ 5 | 100 | d |

---

## 3. Manejo de errores

Se manejan tres tipos de error: mensajes **vacíos o nulos**, **claves inválidas** y **caracteres no soportados**. Se fuerzan los fallos y se capturan con `match` sobre `Success` / `Failure`, sin detener el programa.

```scala
// 3. Manejo de errores
def mostrar(r: Try[String]): String = r match {
  case Success(t) => s"OK: $t"
  case Failure(e) => s"Error capturado: ${e.getMessage}"
}

mostrar(procesar_mensaje("", 3, cifrado_cesar))             // Error capturado: El mensaje es nulo o esta vacio
mostrar(procesar_mensaje(null, 3, cifrado_cesar))           // Error capturado: El mensaje es nulo o esta vacio
mostrar(procesar_mensaje("HOLA", 999999, cifrado_cesar))    // Error capturado: Clave invalida: 999999
mostrar(procesar_mensaje("Hola Mundo", 3, cifrado_cesar))   // Error capturado: Caracter no soportado: ' '
mostrar(procesar_mensaje("hola", 200, cifrado_xor))         // Error capturado: Clave XOR invalida: 200
mostrar(procesar_mensaje("Ñandú", 5, cifrado_xor))          // Error capturado: Caracter no ASCII: 'Ñ'
```

---

## Preguntas para reflexionar

**¿Por qué XOR es su propia inversa con el mismo `^`, mientras que el César requiere invertir el signo de la clave?**
Porque `(a ⊕ k) ⊕ k = a ⊕ 0 = a`: todo bit XOR consigo mismo da 0. En el César se suma, y `(x + k) + k = x + 2k`, que no regresa a `x`; hay que sumar `-k`.

**¿Qué ventajas ofrece `map` sobre un bucle `for` tradicional?**
Es declarativo, no usa variables mutables ni acumuladores, devuelve una colección nueva inmutable y el código es más corto y fácil de componer.

**¿Cómo ayuda el tipado estático al pasar funciones como argumentos?**
El compilador verifica que la función tenga la firma `(Char, Int) => Char`; si no coincide, el error aparece al compilar y no al ejecutar.
