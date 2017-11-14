package com.connorottenbacher.battleship

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ListView
import com.google.gson.Gson
import java.io.File

class MainActivity : AppCompatActivity() {
    lateinit var listView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        renderList()
    }

    override fun onResume() {
        super.onResume()
        renderList()
    }

    //Begins a new game and increments the file number used to keep track of and save unique games
    fun onNewGameClick(view: View){
        //Increments file number and sets the file name
        val sharedPref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = sharedPref.edit()
        val fileNum = sharedPref.getInt("fileNum", 0)+1
        val fileName = "game"+fileNum.toString()+".ott"
        editor.putInt("fileNum", fileNum)
        editor.putString("currentFile", fileName)
        editor.apply()
        //Creates a new empty file for the new game
        val directory: File = filesDir
        val file: File = File(directory, fileName)
        file.createNewFile()
        val intent = Intent(this, GameActivity::class.java)
        startActivity(intent)
    }

    fun delete(view: View){
        val fileName: String = view.tag.toString()
        val directory: File = filesDir
        val file: File = File(directory, fileName)
        file.delete()
        renderList()
    }

    fun renderList(){
        val files: File = filesDir
        var metaData: ArrayList<ArrayList<String>> = arrayListOf<ArrayList<String>>()
        for(file in files.listFiles()){
            val gson: Gson = Gson()
            val game: Game = gson.fromJson(file.readText(), Game::class.java)
            var data: ArrayList<String> = arrayListOf<String>()
            var turn: String = if(game.p1Turn) "Turn: P1, " else "Turn: P2, "
            var p1Ships: String = "P1 Ships Remaining: " + game.p1.shipsLeft.toString()+ ", "
            var p2Ships: String = "P2 Ships Remaining: " + game.p2.shipsLeft.toString()
            var gameState: String = when(game.gameState){
                Game.GameState.INPROGRESS -> "In Progress, "
                Game.GameState.STARTING -> "New Game, "
                Game.GameState.P2VICTORY -> "P2 Won, "
                Game.GameState.P1VICTORY -> "P1 Won, "
            }
            data.add(file.name)
            data.add(gameState)
            data.add(turn)
            data.add(p1Ships)
            data.add(p2Ships)
            metaData.add(data)
        }
        val adapter: CustomAdapter = CustomAdapter(this, metaData)
        listView = findViewById<ListView>(R.id.listView)
        listView.adapter = adapter

        //When a list item is clicked, get the right file and starts the game
        listView.setOnItemClickListener { adapterView, view, i, l ->
            val fileName: String = (view.tag as CustomAdapter.ViewHolder)?.delete?.tag.toString()
            val sharedPref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
            val editor = sharedPref.edit()
            editor.putString("currentFile", fileName)
            editor.apply()
            val intent = Intent(this, GameActivity::class.java)
            startActivity(intent)
        }
    }
}
