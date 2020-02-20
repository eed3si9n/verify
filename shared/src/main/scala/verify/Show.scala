package verify

trait Show[T] extends Serializable {
  def show(t: T): String
}

object Show {
  def apply[A](implicit instance: Show[A]): Show[A] = instance

  implicit def toStringShow[T](implicit refute: Refute[Show[T]]): Show[T] = new Show[T] {
    override def show(t: T): String = if (t == null) "null" else t.toString
  }
}
