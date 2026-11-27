# Mini Proyecto: Higher Order Functions en Criptografía

**Programación Funcional y Reactiva**

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
El motor funciona en tres pasos:

1. **Valida el mensaje:** si es nulo o vacío, devuelve `Failure`.
2. **Valida la clave:** si está fuera del rango permitido, devuelve `Failure`.
3. **Cifra:** aplica con `map` la función `cifrado` a cada carácter. Está dentro de `Try`, así que si la función lanza una excepción (por ejemplo, un carácter no soportado), se captura como `Failure`.

### Cifrado César

#### ¿Cómo funciona?

El César es un cifrado por **sustitución**: cada letra se reemplaza por otra que está `k` posiciones más adelante en el alfabeto. La clave `k` indica cuántos lugares se desplaza.

1. Cada letra se convierte en su posición en el alfabeto (`A = 0`, `B = 1`, ..., `Z = 25`).
2. Se suma la clave `k`.
3. Se aplica `mod 26` para que, si se pasa de la `Z`, **dé la vuelta** al inicio del alfabeto.
4. La nueva posición se convierte otra vez en letra.

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
- `c - base` obtiene la posición de la letra en el alfabeto (restando el código de `'A'` o de `'a'`).
- `base + ...` convierte la nueva posición otra vez en letra.
- Se usa `'A'` como base para mayúsculas y `'a'` para minúsculas, así se conserva el formato original.
- Cualquier otro carácter lanza una excepción.

Ejemplo con `"AD"` y `k = 1`: `A (0) → (0+1) mod 26 = 1 → B` y `D (3) → (3+1) mod 26 = 4 → E`, resultado `"BE"`.

### Cifrado XOR

#### ¿Cómo funciona?

El XOR (*Exclusive OR*, "o exclusivo") trabaja con los **bits** del código ASCII de cada carácter. Compara dos bits y devuelve `1` si son **distintos** y `0` si son **iguales**:

| Bit A | Bit B | A ⊕ B |
|:-----:|:-----:|:-----:|
| 0 | 0 | 0 |
| 0 | 1 | 1 |
| 1 | 0 | 1 |
| 1 | 1 | 0 |

Para cifrar un carácter:

1. Se obtiene su código ASCII (por ejemplo `'s' = 115`).
2. Se pasa a binario junto con la clave (`115 = 0111 0011` y `5 = 0000 0101`).
3. Se aplica XOR bit a bit (`0111 0011 ⊕ 0000 0101 = 0111 0110`).
4. El resultado se convierte otra vez en carácter (`0111 0110 = 118 = 'v'`).
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
- Primero valida que la clave esté entre `0` y `127` y que el carácter sea ASCII.
- `c ^ k` aplica el XOR bit a bit con el operador `^` y `.toChar` convierte el resultado en carácter.

---

## 2. Pruebas de cifrado y descifrado

### Caso César: `"FUNCIONAL"` con `k = 3`

```scala
//2.1 Caso Cesar
val cesar_cifrado = procesar_mensaje("FUNCIONAL", 3, cifrado_cesar)
//resultado: Success(IXQFLRQDO)

// Descifrar con clave -3 (logica inversa)
val cesar_descifrado = cesar_cifrado.flatMap(t => procesar_mensaje(t, -3, cifrado_cesar))
//Resultado: Success(FUNCIONAL)
```
Cada letra avanza 3 posiciones al cifrar. Para descifrar se usa `flatMap`, que toma el texto cifrado (solo si fue `Success`) y le aplica el desplazamiento contrario con `-3`.

| F | U | N | C | I | O | N | A | L |
|:-:|:-:|:-:|:-:|:-:|:-:|:-:|:-:|:-:|
| I | X | Q | F | L | R | Q | D | O |

### Caso XOR: `"scala"` con `k = 5`

Paso detallado para `'s'`: `115 ^ 5` → `0111 0011 ⊕ 0000 0101 = 0111 0110` = `118` = `'v'`.

```scala
// 2.2 Caso XOR (operador ^ dentro de la lambda)
val xor_cifrado = procesar_mensaje("scala", 5, (c, k) => (c ^ k).toChar)
// resultado: Success(vfdid)

// Misma funcion y misma clave para recuperar el original
val xor_descifrado = xor_cifrado.flatMap(t => procesar_mensaje(t, 5, (c, k) => (c ^ k).toChar))
// resultado: Success(scala)
```
La función se pasa directamente como **lambda** con el operador `^` dentro. Al aplicarla de nuevo con la misma clave `k = 5`, se recupera `scala`.

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
  case Success(t) => s"correcto: $t"
  case Failure(e) => s"Error capturado: ${e.getMessage}"
}

mostrar(procesar_mensaje("", 3, cifrado_cesar))             // Error capturado: El mensaje es nulo o esta vacio
mostrar(procesar_mensaje(null, 3, cifrado_cesar))           // Error capturado: El mensaje es nulo o esta vacio
mostrar(procesar_mensaje("HOLA", 999999, cifrado_cesar))    // Error capturado: Clave invalida: 999999
mostrar(procesar_mensaje("Hola a todos", 3, cifrado_cesar))   // Error capturado: Caracter no soportado: ' '
mostrar(procesar_mensaje("hola", 200, cifrado_xor))         // Error capturado: Clave XOR invalida: 200
mostrar(procesar_mensaje("Ñaño", 5, cifrado_xor))          // Error capturado: Caracter no ASCII: 'Ñ'
```
<img width="935" height="212" alt="image" src="https://github.com/user-attachments/assets/298bbaa5-191b-4503-a7c0-3dcd55e92b86" />

---

## Preguntas para reflexionar

**¿Por qué XOR es su propia inversa con el mismo `^`, mientras que el César requiere invertir el signo de la clave?**

Porque XOR permite recuperar el mensaje original aplicando nuevamente la misma operación con la misma clave y por otra parte en Cesar desplaza las letras en una dirección y necesita desplazarlas en la dirección contraria para recuperar el mensaje original.

**¿Qué ventajas ofrece `map` sobre un bucle `for` tradicional?**

map permite recorrer y transformar cada caracter de un mensaje de manera sencilla sin modificar el mensaje original y facilita la programación funcional y permite escribir código más corto y claro.

**¿Cómo ayuda el tipado estático al pasar funciones como argumentos?**

El tipado estático comprueba que los datos y las funciones tengan los tipos correctos como podria ser el permitir verificar que la función de cifrado reciba un Char y un Int, y devuelva un Char, evitando errores al utilizarla.
