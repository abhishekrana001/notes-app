package com.example.notesapp

import android.app.AlertDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

data class Note(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

class NotesAdapter(
    private var notes: MutableList<Note>,
    private val onDeleteClick: (Note) -> Unit
) : RecyclerView.Adapter<NotesAdapter.NoteViewHolder>() {

    class NoteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleText: TextView = view.findViewById(R.id.noteTitle)
        val contentText: TextView = view.findViewById(R.id.noteContent)
        val timestampText: TextView = view.findViewById(R.id.noteTimestamp)
        val deleteIcon: View = view.findViewById(R.id.deleteIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_note, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = notes[position]
        holder.titleText.text = note.title
        holder.contentText.text = note.content

        val dateFormat = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
        holder.timestampText.text = dateFormat.format(Date(note.timestamp))

        holder.deleteIcon.setOnClickListener {
            onDeleteClick(note)
        }
    }

    override fun getItemCount() = notes.size

    fun updateNotes(newNotes: List<Note>) {
        notes.clear()
        notes.addAll(newNotes.sortedByDescending { it.timestamp })
        notifyDataSetChanged()
    }
}

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NotesAdapter
    private lateinit var fab: FloatingActionButton
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var emptyStateText: TextView
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        enableEdgeToEdge()

        initViews()
        setupRecyclerView()
        loadNotes()

        fab.setOnClickListener { showAddNoteDialog() }
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerView)
        fab = findViewById(R.id.fab)
        emptyStateText = findViewById(R.id.emptyStateText)
        sharedPreferences = getSharedPreferences("notes_prefs", MODE_PRIVATE)
    }

    private fun setupRecyclerView() {
        adapter = NotesAdapter(mutableListOf()) { note ->
            showDeleteConfirmationDialog(note)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun loadNotes() {
        val notesJson = sharedPreferences.getString("notes", "[]")
        val type = object : TypeToken<List<Note>>() {}.type
        val notes: List<Note> = gson.fromJson(notesJson, type) ?: emptyList()

        adapter.updateNotes(notes)
        updateEmptyState(notes.isEmpty())
    }

    private fun saveNotes(notes: List<Note>) {
        val notesJson = gson.toJson(notes)
        sharedPreferences.edit().putString("notes", notesJson).apply()
    }

    private fun showAddNoteDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_note, null)
        val titleEdit = dialogView.findViewById<EditText>(R.id.editTitle)
        val contentEdit = dialogView.findViewById<EditText>(R.id.editContent)

        AlertDialog.Builder(this)
            .setTitle("Add New Note")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val title = titleEdit.text.toString().trim()
                val content = contentEdit.text.toString().trim()

                if (title.isNotEmpty() && content.isNotEmpty()) {
                    addNote(title, content)
                } else {
                    Toast.makeText(this, "Please fill in both title and content", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

    private fun addNote(title: String, content: String) {
        val currentNotes = getCurrentNotes().toMutableList()
        val newNote = Note(title = title, content = content)
        currentNotes.add(newNote)

        saveNotes(currentNotes)
        adapter.updateNotes(currentNotes)
        updateEmptyState(false)

        Toast.makeText(this, "Note added successfully", Toast.LENGTH_SHORT).show()
    }

    private fun getCurrentNotes(): List<Note> {
        val notesJson = sharedPreferences.getString("notes", "[]")
        val type = object : TypeToken<List<Note>>() {}.type
        return gson.fromJson(notesJson, type) ?: emptyList()
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            emptyStateText.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyStateText.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    private fun showDeleteConfirmationDialog(note: Note) {
        AlertDialog.Builder(this)
            .setTitle("Delete Note")
            .setMessage("Are you sure you want to delete \"${note.title}\"?")
            .setPositiveButton("Delete") { _, _ ->
                deleteNote(note)
            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

    private fun deleteNote(note: Note) {
        val currentNotes = getCurrentNotes().toMutableList()
        currentNotes.removeAll { it.id == note.id }

        saveNotes(currentNotes)
        adapter.updateNotes(currentNotes)
        updateEmptyState(currentNotes.isEmpty())

        Toast.makeText(this, "Note deleted", Toast.LENGTH_SHORT).show()
    }
}

