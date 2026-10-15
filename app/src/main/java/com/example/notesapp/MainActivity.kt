package com.example.notesapp

import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class Note(
    val id: String = UUID.randomUUID().toString(),
    var title: String,
    var content: String,
    val timestamp: Long = System.currentTimeMillis(),
)

class NotesAdapter(
    private var notes: MutableList<Note>,
    private val onNoteClick: (Note) -> Unit,
    private val onDeleteClick: (Note) -> Unit,
) : RecyclerView.Adapter<NotesAdapter.NoteViewHolder>() {

    class NoteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleText: TextView = view.findViewById(R.id.noteTitle)
        val contentText: TextView = view.findViewById(R.id.noteContent)
        val timestampText: TextView = view.findViewById(R.id.noteTimestamp)
        val editIcon: ImageView = view.findViewById(R.id.editIcon)
        val deleteIcon: ImageView = view.findViewById(R.id.deleteIcon)
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

        holder.itemView.setOnClickListener { onNoteClick(note) }
        holder.editIcon.setOnClickListener { onNoteClick(note) }
        holder.deleteIcon.setOnClickListener { onDeleteClick(note) }
    }

    override fun getItemCount(): Int = notes.size

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
    private lateinit var notesCountText: TextView
    private lateinit var searchEditText: EditText
    private lateinit var clearSearchIcon: ImageView
    private lateinit var emptyStateLayout: View
    private lateinit var emptyStateTitle: TextView
    private lateinit var emptyStateText: TextView
    private lateinit var headerContainer: View

    private val allNotes = mutableListOf<Note>()
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupWindowInsets()
        initViews()
        setupRecyclerView()
        setupSearch()
        loadNotes()

        fab.setOnClickListener { showAddOrEditNoteDialog(null) }
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainRoot)) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            headerContainer.setPadding(
                headerContainer.paddingLeft,
                systemBars.top + 16,
                headerContainer.paddingRight,
                headerContainer.paddingBottom
            )
            val fabMargin = (24 * resources.displayMetrics.density).toInt()
            val layoutParams = fab.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.bottomMargin = fabMargin + systemBars.bottom
            fab.layoutParams = layoutParams
            insets
        }
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerView)
        fab = findViewById(R.id.fab)
        notesCountText = findViewById(R.id.notesCountText)
        searchEditText = findViewById(R.id.searchEditText)
        clearSearchIcon = findViewById(R.id.clearSearchIcon)
        emptyStateLayout = findViewById(R.id.emptyStateLayout)
        emptyStateTitle = findViewById(R.id.emptyStateTitle)
        emptyStateText = findViewById(R.id.emptyStateText)
        headerContainer = findViewById(R.id.headerContainer)

        sharedPreferences = getSharedPreferences("notes_prefs", MODE_PRIVATE)
    }

    private fun setupRecyclerView() {
        adapter = NotesAdapter(
            notes = mutableListOf(),
            onNoteClick = { note -> showAddOrEditNoteDialog(note) },
            onDeleteClick = { note -> showDeleteConfirmationDialog(note) },
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun setupSearch() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString()?.trim().orEmpty()
                clearSearchIcon.visibility = if (query.isNotEmpty()) View.VISIBLE else View.GONE
                filterNotes(query)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        clearSearchIcon.setOnClickListener {
            searchEditText.text.clear()
        }
    }

    private fun loadNotes() {
        val notesJson = sharedPreferences.getString("notes", "[]")
        val type = object : TypeToken<List<Note>>() {}.type
        val loadedNotes: List<Note> = gson.fromJson(notesJson, type) ?: emptyList()

        allNotes.clear()
        allNotes.addAll(loadedNotes)

        filterNotes(searchEditText.text.toString().trim())
    }

    private fun filterNotes(query: String) {
        val filtered = if (query.isEmpty()) {
            allNotes
        } else {
            allNotes.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.content.contains(query, ignoreCase = true)
            }
        }

        adapter.updateNotes(filtered)
        updateUI(filtered, query)
    }

    private fun updateUI(filteredNotes: List<Note>, query: String) {
        val totalCount = allNotes.size
        val displayedCount = filteredNotes.size

        notesCountText.text = if (query.isEmpty()) {
            when (totalCount) {
                1 -> getString(R.string.notes_count_singular)
                else -> getString(R.string.notes_count_plural, totalCount)
            }
        } else {
            getString(R.string.notes_found_count, displayedCount)
        }

        if (displayedCount == 0) {
            emptyStateLayout.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE

            if (query.isNotEmpty()) {
                emptyStateTitle.text = getString(R.string.no_results_title)
                emptyStateText.text = getString(R.string.no_results_desc)
            } else {
                emptyStateTitle.text = getString(R.string.no_notes_title)
                emptyStateText.text = getString(R.string.no_notes_desc)
            }
        } else {
            emptyStateLayout.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    private fun saveNotes() {
        val notesJson = gson.toJson(allNotes)
        sharedPreferences.edit().putString("notes", notesJson).apply()
    }

    private fun showAddOrEditNoteDialog(noteToEdit: Note?) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_note, null)
        val titleEdit = dialogView.findViewById<EditText>(R.id.editTitle)
        val contentEdit = dialogView.findViewById<EditText>(R.id.editContent)

        if (noteToEdit != null) {
            titleEdit.setText(noteToEdit.title)
            contentEdit.setText(noteToEdit.content)
        }

        val dialogTitleRes = if (noteToEdit != null) R.string.edit_note_title else R.string.add_note_title
        val positiveButtonRes = if (noteToEdit != null) R.string.update_button else R.string.save_button

        MaterialAlertDialogBuilder(this)
            .setTitle(dialogTitleRes)
            .setView(dialogView)
            .setPositiveButton(positiveButtonRes) { _, _ ->
                val title = titleEdit.text.toString().trim()
                val content = contentEdit.text.toString().trim()

                if (title.isNotEmpty() && content.isNotEmpty()) {
                    if (noteToEdit != null) {
                        updateNote(noteToEdit, title, content)
                    } else {
                        addNote(title, content)
                    }
                } else {
                    Toast.makeText(this, getString(R.string.empty_fields_warning), Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(R.string.cancel_button, null)
            .create()
            .show()
    }

    private fun addNote(title: String, content: String) {
        val newNote = Note(title = title, content = content)
        allNotes.add(0, newNote)

        saveNotes()
        filterNotes(searchEditText.text.toString().trim())

        Toast.makeText(this, getString(R.string.note_added_msg), Toast.LENGTH_SHORT).show()
    }

    private fun updateNote(note: Note, title: String, content: String) {
        val index = allNotes.indexOfFirst { it.id == note.id }
        if (index != -1) {
            allNotes[index].title = title
            allNotes[index].content = content

            saveNotes()
            filterNotes(searchEditText.text.toString().trim())

            Toast.makeText(this, getString(R.string.note_updated_msg), Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDeleteConfirmationDialog(note: Note) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.delete_note_title)
            .setMessage(getString(R.string.delete_confirm_msg, note.title))
            .setPositiveButton(R.string.delete_button) { _, _ ->
                deleteNote(note)
            }
            .setNegativeButton(R.string.cancel_button, null)
            .create()
            .show()
    }

    private fun deleteNote(note: Note) {
        allNotes.removeAll { it.id == note.id }

        saveNotes()
        filterNotes(searchEditText.text.toString().trim())

        Toast.makeText(this, getString(R.string.note_deleted_msg), Toast.LENGTH_SHORT).show()
    }
}
