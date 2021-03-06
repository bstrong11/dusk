package rs.dusk.game.world.map

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since December 17, 2020
 */
data class Tile(val x : Int, val y : Int, val plane : Int) {
	
	fun getChunkX() : Int {
		return x shr 3
	}
	
	fun getChunkY() : Int {
		return y shr 3
	}
	
	fun getRegionId() : Int {
		return (getRegionX() shl 8) + getRegionY()
	}
	
	fun getRegionX() : Int {
		return x shr 6
	}
	
	fun getRegionY() : Int {
		return y shr 6
	}
	
	fun get18BitsLocationHash() : Int {
		return getRegionY() + (getRegionX() shl 8) + (plane shl 16)
	}
	
	fun get30BitsLocationHash() : Int {
		return y + (x shl 14) + (plane shl 28)
	}
	
	companion object {
		
		fun getId(x : Int, y : Int, plane : Int = 0) =
			(y and 0x3fff) + ((x and 0x3fff) shl 14) + ((plane and 0x3) shl 28)
		
		val DEFAULT = Tile(3092, 3503, 0)
	}
}