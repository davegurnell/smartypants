package smartypants

class smart[A](name: String = "") extends scala.annotation.StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro Macros.constructorMacro
}
