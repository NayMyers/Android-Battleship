package com.connorottenbacher.battleship

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.connorottenbacher.battleship.Constants.Companion.SHIP
import java.io.File
import java.security.SecureRandom

//Keys used to identify ships
const val five: String = "five"
const val four: String = "four"
const val three: String = "three"
const val otherthree: String = "otherthree"
const val two: String = "two"

class Game(@Transient val context: Context) {

    var p1: Player = Player()
    var p2: Player = Player()

    @Transient
    val random: SecureRandom = SecureRandom()

    var p1Turn: Boolean = true;

    var shipSunk: Boolean = false;

    var gameState: GameState = GameState.STARTING

    public fun attack(row: Int, column: Int): Boolean {
        gameState = GameState.INPROGRESS
        var opponent = if (p1Turn) p2 else p1
        val location = opponent.board[row][column]
        when (location) {
            Constants.EMPTY -> {
                opponent.board[row][column] = Constants.MISSED
                return false
            }
            Constants.SHIP -> {
                //A ship is hit. Marks the board appropriately and updates the ships hit points.
                //Notifies of a recently sunk ship if appropriate
                opponent.board[row][column] = Constants.HIT_SHIP
                val shipString = row.toString() + column.toString()
                val hitKey = opponent.shipLocations.get(shipString)
                opponent.shipHits.put(hitKey!!, opponent.shipHits.getValue(hitKey) - 1)
                if (opponent.shipHits.getValue(hitKey) == 0) {
                    shipSunk = true
                    opponent.shipsLeft--
                    //Updates hits to sunk
                    for (location in opponent.shipLocations) {
                        if (location.value == hitKey) {
                            val r = location.key[0].toString().toInt()
                            val c = location.key[1].toString().toInt()
                            opponent.board[r][c] = Constants.SUNK_SHIP
                        }
                    }
                }

                return true
            }
            else -> {
                return false
            }
        }
    }

    //Returns whether or not all ships have been sunk
    public fun checkVictory(): Boolean {
        val opponent = if (p1Turn) p2 else p1
        var victory = true
        for (ships in opponent.shipHits) {
            if (ships.value > 0) {
                victory = false
                break
            }
        }
        if(victory) gameState = if(p1Turn) GameState.P1VICTORY else GameState.P2VICTORY
        return victory
    }

    public fun checkSunkShip(): Boolean {
        if (shipSunk) {
            shipSunk = false
            return true
        } else {
            return false
        }
    }

    public fun placeRandomShips() {
        placeShip(5, p1, five)
        placeShip(4, p1, four)
        placeShip(3, p1, three)
        placeShip(3, p1, otherthree)
        placeShip(2, p1, two)

        placeShip(5, p2, five)
        placeShip(4, p2, four)
        placeShip(3, p2, three)
        placeShip(3, p2, otherthree)
        placeShip(2, p2, two)
    }

    //Helper function gets a random location to place a ship either horizontally or vertically of the given size
    private fun placeShip(size: Int, player: Player, key: String) {
        val horizontal = if ((random.nextInt() % 2) == 0) false else true
        val randomX = if (horizontal) random.nextInt(10 - size) else random.nextInt(10)
        val randomY = if (horizontal) random.nextInt(10) else random.nextInt(10 - size)

        var clear: Boolean = true
        //Checks that the area is clear to place a ship
        for (i in 0..size - 1) {
            val x = if (horizontal) randomX + i else randomX
            val y = if (horizontal) randomY else randomY + i
            if (player.board[x][y] != 0) {
                clear = false
                break;
            }
        }
        //Places the ship or calls this function again to find a suitable place for the ship
        if (clear) {
            for (i in 0..size - 1) {
                val x = if (horizontal) randomX + i else randomX
                val y = if (horizontal) randomY else randomY + i
                player.board[x][y] = SHIP
                val shipString = x.toString() + y.toString()
                player.shipLocations.set(shipString, key)
            }
        } else {
            placeShip(size, player, key)
        }
    }

    public fun takeTurn() {
        p1Turn = !p1Turn
    }

    //Saves this games data to a text file in a JSON format
    public fun saveGame() {
        val sharedPref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = sharedPref.edit()
        val fileNum = sharedPref.getInt("fileNum", 0)
        val fileName = "game" + fileNum.toString() + ".ott"
        var file: File = File(context.filesDir, fileName)
        file.createNewFile()
    }


    //Game boards are represented by arrays of integers. Integers will represent either a ship, hit ship, empty space, or a miss
    //Constants for these representations are stored in Constants class
    //A Players ships are represented by a hash map. Keys are xy coordinates as a string.
    // Values are keys to a map containing the hitpoints of the ship hit
    class Player {
        val board: Array<IntArray> = Array(10, { IntArray(10) })
        var shipLocations: MutableMap<String, String> = mutableMapOf()
        var shipHits: MutableMap<String, Int> = mutableMapOf(Pair(five, 5), Pair(four, 4), Pair(three, 3), Pair(otherthree, 3), Pair(two, 2))
        var shipsLeft: Int = 5
    }

    enum class GameState{
        STARTING, INPROGRESS, P1VICTORY, P2VICTORY
    }
}