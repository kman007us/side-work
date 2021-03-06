trait RNG {
	def nextInt: (Int, RNG)
}

// @author: `Functional Programming in Scala` 
// www.manning.com/bjarnason/

object RNG {
	def simple(seed: Long): RNG = new RNG {
		def nextInt: (Int, RNG) = {
			val seed2 = (seed*0x5DEECE66DL + 0xBL) &
						((1L << 48) - 1)
			((seed2 >>> 16).asInstanceOf[Int],
				simple(seed2))
		}
	}
}