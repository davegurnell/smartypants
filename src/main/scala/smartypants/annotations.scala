package smartypants

import scala.annotation.StaticAnnotation

class smart[A](name: String = "") extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro Macros.smartMacro
}

class smarts[A](prefix: String = "") extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro Macros.smartsMacro
}
