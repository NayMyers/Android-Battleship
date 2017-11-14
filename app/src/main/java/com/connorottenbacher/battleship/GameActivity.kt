package com.connorottenbacher.battleship

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Point
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.*
import com.google.gson.Gson
import java.io.File
import java.io.FileOutputStream



class GameActivity : AppCompatActivity() {
    //Player boards are represented by tables
    lateinit var yourBoard: TableLayout
    lateinit var opponentBoard: TableLayout
    lateinit var attackBtn: Button
    var sharedPref: SharedPreferences? = null
    var currentFile: String? = null
    val gson: Gson = Gson()
    var game: Game = Game(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        currentFile = sharedPref?.getString("currentFile", "oh no")
        yourBoard = findViewById<TableLayout>(R.id.your_board)
        opponentBoard = findViewById<TableLayout>(R.id.opponent_board)
        attackBtn = findViewById<Button>(R.id.attack)

        load()
    }

    //Takes a turn and pauses the game until the current player presses 'ok'
    fun takeTurn(){
        game.takeTurn()
        yourBoard.removeAllViews()
        opponentBoard.removeAllViews()
        findViewById<EditText>(R.id.row).setText("")
        findViewById<EditText>(R.id.column).setText("")
        findViewById<LinearLayout>(R.id.attackControl).setVisibility(View.GONE)
        val currentPlayer = if(game.p1Turn) "player 1" else "player 2"
        showAlert("CHANGE TURNS", "Pass the device to $currentPlayer", {updateView()})
        save()
    }

    //Performs an attack and informs the player of the result via dialog
    fun attack(view: View){
        val rowInput = (findViewById<EditText>(R.id.row)).text.toString()
        val colInput = (findViewById<EditText>(R.id.column)).text.toString()
        if(rowInput.isEmpty() || colInput.isEmpty()){
            showAlert("MISFIRE", "You must enter both a row and column coordinate", {})
            return
        }
        val row = rowInput.toInt() - 1
        val column = colInput.toInt() - 1
        if(row !in 0..9 || column !in 0..9){
            showAlert("BAD SHOT", "Your coordinates must be between 1 and 10", {})
            return
        }
        val hit = game.attack(row, column)
        if(game.checkVictory()){
            val victor = if(game.p1Turn) "Player 1 " else "Player 2 "
            showAlert("VICTORY", victor + "is victorious!", {victory()})
        }
        else if(game.checkSunkShip()){
            showAlert("SUNK", "You sunk an opposing ship!", {takeTurn()})
        }
        else if(hit){
            showAlert("HIT", "You hit an opposing ship!", {takeTurn()})
        }
        else{
            showAlert("MISS", "You missed!", {takeTurn()})
        }
    }

    fun save(){
        var fos: FileOutputStream = openFileOutput(currentFile, Context.MODE_PRIVATE)
        val gameString: String = gson.toJson(game)
        fos.write(gameString.toByteArray())
        fos.close()
    }

    //Loads from the current file and sets the game board
    fun load(){
        val directory: File = filesDir
        val file: File = File(directory, currentFile)
        val saved = file.readText()
        //New game
        if (saved.isEmpty()) {
            game = Game(this);
            game.placeRandomShips()
            save()
            //Old game
        } else {
            game = gson.fromJson(saved, Game::class.java)
        }
        updateView()
    }

    //Shows a dialog with the specified title, message, and a callback function called upon clicking 'ok'
    private fun showAlert(title: String, message: String, callback: ()-> Unit){
        var alert = AlertDialog.Builder(this)
        alert.setTitle(title)
        alert.setMessage(message)
        alert.setPositiveButton("OK"){
            dialog, button -> callback()
        }
        alert.show()
    }

    //Victory was achieved. Disables further action from players
    fun victory(){
        findViewById<LinearLayout>(R.id.attackControl).setVisibility(View.GONE)
        save()
    }

    //Updates the view of the boards which will show the current players ship positions and hits
    //on the opposing players ships
    private fun updateView(){
        val currentPlayer: Array<IntArray> = if(game.p1Turn) game.p1.board else game.p2.board
        val opponent: Array<IntArray> = if(game.p1Turn) game.p2.board else game.p1.board
        //Gets the current screen width and determines a tile size
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        //Gets a width based on whether or not the screen is in portrait mode
        val width = if(size.x < size.y) size.x/20 else size.y/20
        //Updates the current players board
        for(i in 0..10){
            val row: TableRow = TableRow(this);
            yourBoard.addView(row)
            //Adds the column numbers rather than the game tiles
            if(i == 0){
                for(k in 0..10){
                    val tile: TextView = TextView(this)
                    if(k != 0) tile.setText(k.toString()) else tile.setText("")
                    row.addView(tile)
                }
                continue;
            }
            for(j in 0..10){
                //Adds the row numbers rather than the game tiles
                if(j == 0){
                    val tile: TextView = TextView(this)
                    tile.setText(i.toString())
                    row.addView(tile)
                    continue;
                }
                val tile: ImageView = ImageView(this)
                tile.setPadding(width, width, 0, 0)
                when(currentPlayer[i-1][j-1]){
                    Constants.HIT_SHIP -> tile.setBackgroundResource(R.drawable.grid_item_red)
                    Constants.SHIP -> tile.setBackgroundResource(R.drawable.grid_item_gray)
                    Constants.SUNK_SHIP -> tile.setBackgroundResource(R.drawable.grid_item_black)
                    else -> tile.setBackgroundResource(R.drawable.grid_item_blue)
                }
                row.addView(tile)
            }
        }
        //Updates the opposing players board
        for(i in 0..10){
            val row: TableRow = TableRow(this);
            opponentBoard.addView(row)
            //Adds the column numbers rather than the game tiles
            if(i == 0){
                for(k in 0..10){
                    val tile: TextView = TextView(this)
                    if(k != 0) tile.setText(k.toString()) else tile.setText("")
                    row.addView(tile)
                }
                continue;
            }
            for(j in 0..10) {
                //Adds the row numbers rather than the game tiles
                if(j == 0){
                    val tile: TextView = TextView(this)
                    tile.setText(i.toString())
                    row.addView(tile)
                    continue;
                }
                val tile: ImageView = ImageView(this)
                tile.setPadding(width, width, 0, 0)
                when(opponent[i-1][j-1]){
                    Constants.HIT_SHIP -> tile.setBackgroundResource(R.drawable.grid_item_red)
                    Constants.MISSED -> tile.setBackgroundResource(R.drawable.grid_item_white)
                    Constants.SUNK_SHIP -> tile.setBackgroundResource(R.drawable.grid_item_black)
                    else -> tile.setBackgroundResource(R.drawable.grid_item_blue)
                }
                row.addView(tile)
            }
        }
        //If a player has yet to win, reveals the attack button
        if(game.gameState != Game.GameState.P1VICTORY && game.gameState != Game.GameState.P2VICTORY){
            findViewById<LinearLayout>(R.id.attackControl).setVisibility(View.VISIBLE)
        }
        else{
            findViewById<LinearLayout>(R.id.attackControl).setVisibility(View.GONE)
        }
    }
}
