trait Functor[F[_]] {
  def map[A,B](fa: F[A])(f: A => B): F[B]
}

trait Monad[F[_]] extends Functor[F] {
	def unit[A](a: => A): F[A]
	def flatMap[A,B](ma: F[A])(f: A => F[B]): F[B]

	def map[A,B](ma: F[A])(f: A => B): F[B] = 
		flatMap(ma)(a => unit(f(a)))
	def map2[A, B, C](ma: F[A], mb: F[B])(f: (A,B) => C): F[C] = 
		flatMap(ma)(a => map(mb)(b => f(a, b)))

	/* Wrong answers originally for sequence & traverse due to http://stackoverflow.com/a/20037817/409976 */

	// Exercise 3: implement sequence() and traverse
	// official answer from @pchiusano
	def sequence[A](lma: List[F[A]]): F[List[A]] = 
		lma.foldRight(unit(List[A]()))((ma, mla) => map2(ma, mla)(_ :: _))

	def traverse[A, B](la: List[A])(f: A => F[B]): F[List[B]] =
		la.foldRight(unit(List[B]()))((ma, mla) => map2(f(ma), mla)(_ :: _))

	// Simple predecessor to replicateM to help me understand how to write replicateM
	def replicateOnce[A](ma: F[A]): F[List[A]] = {
		map(ma)(x => List(x))
	}

	// Exercise 4: implement replicateM
	def replicateM[A](n: Int, ma: F[A]): F[List[A]] = {
		sequence(List.fill(n)(ma))
	}
}

object MonadTest {
	/*val genMonad = new Monad[Gen] {
		def unit[A](a: => A): Gen[A] = Gen.unit(a)
		def flatMap[A, B](ma: Gen[A])(f: A => Gen[B]) = 
			ma.flatMap(f)
	}*/

	val optionMonad = new Monad[Option] {
		def unit[A](a: => A): Option[A] = Some(a)
		def flatMap[A, B](ma: Option[A])(f: A => Option[B]) = 
			ma.flatMap(f) // note ma's type - Option[A]
	}

	val streamMonad = new Monad[Stream] {
		def unit[A](a: => A): Stream[A] = Stream(a)
		def flatMap[A, B](ma: Stream[A])(f: A => Stream[B]) = 
			ma.flatMap(f)
	}

	val listMonad = new Monad[List] {
		def unit[A](a: => A): List[A] = List(a)
		def flatMap[A, B](ma: List[A])(f: A => List[B]) = 
			ma.flatMap(f)
	}

	def main(args: Array[String]) = {
		val optUnit5 = optionMonad.unit(5)
		println(optUnit5)
		assert(optUnit5 == Some(5))

		def foo(x: Int): Option[String] = Some(x.toString)
		val optFlatMap = optionMonad.flatMap(Some(10))(foo)
		println(optFlatMap)
		val expected: Option[String] = Some("10")
		assert(optFlatMap == expected)

		val listOpts = List(Some(1), None, Some(2))
		val seqRes = optionMonad.sequence(listOpts)
		val expected2 = None // having a `None` results in flatMap's call to return None for `acc` & total
		println(seqRes)
		assert(seqRes == expected2)

		val listInt = List(3,2,1)
		def bar(x: Int): Option[String] = Some(x.toString)
		val traverseRes = optionMonad.traverse(listInt)(bar)
		println(traverseRes)
		assert(traverseRes == Some(List("3", "2", "1")))

		val x = Some(2)
		val replicateOnceRes = optionMonad.replicateOnce(x)
		println(replicateOnceRes)
		assert(replicateOnceRes == Some(List(2)))

		val replicateRes = optionMonad.replicateM(3, x)
		println(replicateRes)
		assert(replicateRes == Some(List(2,2,2)))
	}


	/*val parMonad = new Monad[Par] {
		def unit[A](a: => A) = Par.unit(a)
		def flatMap[A, B](ma: Par[A])(f: A => Par[B]): Par[B] = Par.flatmap(ma)(f)
	}*/
}