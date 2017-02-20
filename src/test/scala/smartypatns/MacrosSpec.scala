package smartypants

import org.scalatest._
import shapeless.test.illTyped

object MacrosSpec {
  sealed trait BaseDemo extends Product with Serializable

  object BaseDemo {
    @smart final case object Sub1 extends BaseDemo
    @smart final case class Sub2(a: Int, b: String) extends BaseDemo
    @smart final case class Sub3[A](a: A) extends BaseDemo
  }

  sealed trait RenameDemo extends Product with Serializable

  object RenameDemo {
    @smart("renamed1") final case object Sub1 extends RenameDemo
    @smart("renamed2") final case class Sub2(a: Int, b: String) extends RenameDemo
    @smart("renamed3") final case class Sub3[A](a: A) extends RenameDemo
  }

  sealed trait RetypeDemo extends Product with Serializable
  sealed trait RetypeDemo2 extends RetypeDemo

  object RetypeDemo {
    @smart final case object Sub1 extends RetypeDemo2
    @smart final case class Sub2(a: Int, b: String) extends RetypeDemo2
    @smart final case class Sub3[A](a: A) extends RetypeDemo2
    @smart[RetypeDemo] final case object Sub4 extends RetypeDemo2
    @smart[RetypeDemo] final case class Sub5(a: Int, b: String) extends RetypeDemo2
    @smart[RetypeDemo] final case class Sub6[A](a: A) extends RetypeDemo2
  }

  sealed trait KitchenSinkDemo extends Product with Serializable
  sealed trait KitchenSinkDemo2 extends KitchenSinkDemo

  object KitchenSinkDemo {
    @smart("renamed1") final case object Sub1 extends KitchenSinkDemo2
    @smart("renamed2") final case class Sub2(a: Int, b: String) extends KitchenSinkDemo2
    @smart("renamed3") final case class Sub3[A](a: A) extends KitchenSinkDemo2
    @smart[KitchenSinkDemo]("renamed4") final case object Sub4 extends KitchenSinkDemo2
    @smart[KitchenSinkDemo]("renamed5") final case class Sub5(a: Int, b: String) extends KitchenSinkDemo2
    @smart[KitchenSinkDemo]("renamed6") final case class Sub6[A](a: A) extends KitchenSinkDemo2
  }

  sealed abstract class ConstructorParamDemo(val value: Int) extends Product with Serializable

  object ConstructorParamDemo {
    @smart final case object Sub1 extends ConstructorParamDemo(1)
    @smart final case object Sub2 extends ConstructorParamDemo(2)
  }
}

class MacrosSpec extends FreeSpec with Matchers {
  import MacrosSpec._

  "@smart annotation with no parameters" - {
    "compiles" in {
      BaseDemo.sub1 should be(BaseDemo.Sub1)
      BaseDemo.sub2(123, "abc") should be(BaseDemo.Sub2(123, "abc"))
      BaseDemo.sub3(123) should be(BaseDemo.Sub3(123))
    }

    "generates the correct runtime type" in {
      BaseDemo.sub1.isInstanceOf[BaseDemo.Sub1.type] should be(true)
      BaseDemo.sub2(123, "abc").isInstanceOf[BaseDemo.Sub2] should be(true)
      BaseDemo.sub3(123).isInstanceOf[BaseDemo.Sub3[_]] should be(true)
    }

    "is assignable to supertype" in {
      val a: BaseDemo = BaseDemo.sub1
      val b: BaseDemo = BaseDemo.sub2(123, "abc")
      val c: BaseDemo = BaseDemo.sub3(123)
    }

    "is not assignable to subtype" in {
      illTyped("""val a: BaseDemo.Sub1.type = BaseDemo.sub1""")
      illTyped("""val b: BaseDemo.Sub2 = BaseDemo.sub2(123, "abc")""")
      illTyped("""val c: BaseDemo.Sub3 = BaseDemo.sub3(123)""")
    }
  }

  "@smart annotation with name parameter" - {
    "generates renamed identifiers" in {
      RenameDemo.renamed1 should be(RenameDemo.Sub1)
      RenameDemo.renamed2(123, "abc") should be(RenameDemo.Sub2(123, "abc"))
      RenameDemo.renamed3(123) should be(RenameDemo.Sub3(123))
    }

    "does not generate default identifiers" in {
      illTyped("""RenameDemo.sub1""")
      illTyped("""RenameDemo.sub2(123, "abc")""")
      illTyped("""RenameDemo.sub3(123)""")
    }
  }

  "@smart annotation with type parameter" - {
    "retypes generated smart constructors" in {
      val a: RetypeDemo2 = RetypeDemo.sub1
      val b: RetypeDemo2 = RetypeDemo.sub2(123, "abc")
      val c: RetypeDemo2 = RetypeDemo.sub3(123)
      illTyped("""val d: RetypeDemo2 = RetypeDemo.sub4""")
      illTyped("""val e: RetypeDemo2 = RetypeDemo.sub5(123, "abc")""")
      illTyped("""val f: RetypeDemo2 = RetypeDemo.sub6(123)""")
    }
  }

  "@smart annotation with type parameter and value parameter" - {
    "retypes and renames generated smart constructors" in {
      val a: KitchenSinkDemo2 = KitchenSinkDemo.renamed1
      val b: KitchenSinkDemo2 = KitchenSinkDemo.renamed2(123, "abc")
      val c: KitchenSinkDemo2 = KitchenSinkDemo.renamed3(123)
      illTyped("""val d: RetypeDemo2 = RetypeDemo.renamed4""")
      illTyped("""val e: RetypeDemo2 = RetypeDemo.renamed5(123, "abc")""")
      illTyped("""val f: RetypeDemo2 = RetypeDemo.renamed6(123)""")
    }
  }

  "@smart annotation with supertype with constructor parameters" - {
    "references the correct constructor parameters" in {
      ConstructorParamDemo.sub1.value should be(ConstructorParamDemo.Sub1.value)
      ConstructorParamDemo.sub2.value should be(ConstructorParamDemo.Sub2.value)
    }
  }
}