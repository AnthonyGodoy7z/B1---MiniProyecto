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
procesar_mensaje("hola", 1, (c, k) => (c + k).toChar)
procesar_mensaje("", 1, (c, k) => (c + k).toChar)

// Desplazar letras
def desplazar(c: Char, base: Char, k: Int): Char =
  (base + Math.floorMod(c - base + k, 26)).toChar

// Cifrado cesar
def cifrado_cesar(c: Char, k: Int): Char = {
  if (c >= 'A' && c <= 'Z') desplazar(c, 'A', k)
  else if (c >= 'a' && c <= 'z') desplazar(c, 'a', k)
  else throw new IllegalArgumentException(s"Caracter no soportado: '$c'")
}

//Cifrado XOR
def cifrado_xor(c: Char, k: Int): Char = {
  if (k < 0 || k > 127)
    throw new IllegalArgumentException(s"Clave XOR invalida: $k (debe estar entre 0 y 127)")
  if (c > 127)
    throw new IllegalArgumentException(s"Caracter no ASCII: '$c'")
  (c ^ k).toChar
}

//2.1 Caso Cesar
val cesar_cifrado = procesar_mensaje("FUNCIONAL", 3, cifrado_cesar)
//Sale Success(IXQFLRQDO)

// Descifrar con clave -3 (logica inversa)
val cesar_descifrado = cesar_cifrado.flatMap(t => procesar_mensaje(t, -3, cifrado_cesar))
//Sale Success(FUNCIONAL)

// 2.2 Caso XOR (operador ^ dentro de la lambda)
val xor_cifrado = procesar_mensaje("scala", 5, (c, k) => (c ^ k).toChar)
// resultado: Success(vfdid)

// Misma funcion y misma clave para recuperar el original
val xor_descifrado = xor_cifrado.flatMap(t => procesar_mensaje(t, 5, (c, k) => (c ^ k).toChar))
// resultado: Success(scala)

// 3. Manejo de errores
def mostrar(r: Try[String]): String = r match {
  case Success(t) => s"correcto: $t"
  case Failure(e) => s"Error capturado: ${e.getMessage}"
}

mostrar(procesar_mensaje("", 3, cifrado_cesar))
mostrar(procesar_mensaje(null, 3, cifrado_cesar))
mostrar(procesar_mensaje("HOLA", 999999, cifrado_cesar))
mostrar(procesar_mensaje("Hola a todos", 3, cifrado_cesar))
mostrar(procesar_mensaje("hola", 200, cifrado_xor))
mostrar(procesar_mensaje("Ñaño", 5, cifrado_xor))