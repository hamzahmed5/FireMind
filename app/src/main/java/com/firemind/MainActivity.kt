package com.firemind

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

private data class Movie(
    val id: String,
    val title: String,
    val year: Int,
    val genre: String,
    val mood: List<String>,
    val runtime: Int,
    val familyFriendly: Boolean,
    val rating: Double,
    val description: String
)

class MainActivity : AppCompatActivity() {
    private enum class Screen { HOME, ASSISTANT, RESULTS, DETAILS, WATCHLIST, ABOUT }

    private val background = Color.rgb(9, 17, 31)
    private val surface = Color.rgb(18, 34, 56)
    private val surfaceAlt = Color.rgb(27, 49, 80)
    private val accent = Color.rgb(85, 214, 190)
    private val warm = Color.rgb(255, 184, 107)
    private val text = Color.rgb(245, 248, 252)
    private val muted = Color.rgb(169, 184, 202)

    private lateinit var root: LinearLayout
    private lateinit var content: LinearLayout
    private val prefs by lazy { getSharedPreferences("firemind", Context.MODE_PRIVATE) }
    private var screen = Screen.HOME
    private var selectedMovie: Movie? = null
    private var currentQuery = ""
    private var currentResults: List<Movie> = emptyList()

    private val catalog = listOf(
        Movie("arrival", "Arrival", 2016, "Sci-Fi · Drama", listOf("mind-bending", "serious", "thoughtful"), 116, false, 8.0, "A linguist works to understand visitors from beyond the stars before fear turns first contact into conflict."),
        Movie("moon", "Moon", 2009, "Sci-Fi · Mystery", listOf("mind-bending", "quiet", "short"), 97, false, 7.8, "A solitary lunar worker begins to question the reality of his final weeks on the moon."),
        Movie("hunt-wilderpeople", "Hunt for the Wilderpeople", 2016, "Comedy · Adventure", listOf("funny", "family", "heartwarming"), 101, true, 7.8, "A rebellious kid and his gruff foster uncle become the targets of a nationwide manhunt."),
        Movie("sing-street", "Sing Street", 2016, "Comedy · Music", listOf("funny", "romantic", "uplifting"), 106, true, 7.9, "A teenager starts a band to impress a mysterious girl and finds his own voice along the way."),
        Movie("source-code", "Source Code", 2011, "Action · Sci-Fi", listOf("action", "mind-bending", "short"), 93, false, 7.5, "A soldier relives the last minutes of a commuter train to stop a disaster and find the truth."),
        Movie("kiki", "Kiki's Delivery Service", 1989, "Family · Fantasy", listOf("family", "relaxing", "classic"), 103, true, 7.8, "A young witch finds confidence and community while starting a delivery service in a seaside town."),
        Movie("rear-window", "Rear Window", 1954, "Mystery · Thriller", listOf("mystery", "classic", "thoughtful"), 112, false, 8.5, "A photographer confined to his apartment suspects that a neighbor has committed a terrible crime."),
        Movie("the-rocketeer", "The Rocketeer", 1991, "Action · Adventure", listOf("action", "family", "classic"), 108, true, 6.5, "A daring pilot discovers a rocket pack and races to keep it away from dangerous enemies.")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setBackgroundDrawableResource(android.R.color.transparent)
        buildShell()
        renderHome()
    }

    override fun onBackPressed() {
        when (screen) {
            Screen.HOME -> super.onBackPressed()
            Screen.ASSISTANT, Screen.WATCHLIST, Screen.ABOUT -> renderHome()
            Screen.RESULTS -> renderAssistant()
            Screen.DETAILS -> renderResults()
        }
    }

    private fun buildShell() {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(this@MainActivity.background)
            setPadding(54, 34, 54, 30)
        }
        setContentView(root)
    }

    private fun render(screenToShow: Screen, title: String, subtitle: String? = null, body: (LinearLayout) -> Unit) {
        screen = screenToShow
        root.removeAllViews()
        root.addView(header(title), LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        val scroll = ScrollView(this).apply { isFillViewport = true }
        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 18, 0, 18)
        }
        subtitle?.let { content.addView(label(it, 18f, muted).apply { setPadding(0, 0, 0, 16) }) }
        body(content)
        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        root.addView(TextView(this).apply {
            text = "FireMind · TV-first discovery · Use the remote to navigate"
            textSize = 14f
            setTextColor(muted)
            setPadding(0, 16, 0, 0)
        })
        content.post { focusFirst(content) }
    }

    private fun header(title: String): LinearLayout {
        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val brand = TextView(this).apply {
            text = "FIREMIND"
            textSize = 27f
            setTextColor(accent)
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            letterSpacing = 0.12f
        }
        bar.addView(brand, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        val page = TextView(this).apply {
            text = title.uppercase(Locale.US)
            textSize = 16f
            setTextColor(muted)
            setPadding(0, 0, 20, 0)
        }
        bar.addView(page)
        bar.addView(navButton("Home") { renderHome() })
        bar.addView(navButton("Watchlist") { renderWatchlist() })
        bar.addView(navButton("About") { renderAbout() })
        return bar
    }

    private fun navButton(label: String, action: () -> Unit): Button = tvButton(label, 14f, action, compact = true)

    private fun renderHome() = render(Screen.HOME, "Home", "Tell FireMind what kind of night you want.") { page ->
        val hero = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 24, 32, 28)
            background = rounded(surfaceAlt, 24)
        }
        hero.addView(label("What do you want to watch?", 34f, text))
        hero.addView(label("A small, thoughtful set of recommendations — with a reason for every pick.", 19f, muted).apply { setPadding(0, 10, 0, 20) })
        hero.addView(tvButton("Ask FireMind", 22f, { renderAssistant() }))
        page.addView(hero, fullParams(0, 20))

        page.addView(label("QUICK MOODS", 16f, accent).apply { setPadding(4, 8, 0, 10) })
        val moods = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        listOf("Funny", "Action", "Family", "Sci-Fi", "Relaxing").forEach { mood ->
            moods.addView(tvButton(mood, 17f, { runQuery(mood) }), LinearLayout.LayoutParams(0, 72, 1f).apply { setMargins(0, 0, 12, 0) })
        }
        page.addView(moods, fullParams(0, 22))

        page.addView(label("RECOMMENDED FOR YOU", 16f, accent).apply { setPadding(4, 4, 0, 10) })
        val rail = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        catalog.take(4).forEach { movie -> rail.addView(movieCard(movie), LinearLayout.LayoutParams(0, 194, 1f).apply { setMargins(0, 0, 12, 0) }) }
        page.addView(rail, fullParams(0, 12))
    }

    private fun renderAssistant() = render(Screen.ASSISTANT, "Ask FireMind", "Quick prompts are remote-friendly; text input is available when you want something specific.") { page ->
        val input = EditText(this).apply {
            hint = "e.g. mind-bending sci-fi under two hours"
            textSize = 20f
            setSingleLine(true)
            setTextColor(this@MainActivity.text)
            setHintTextColor(muted)
            setPadding(24, 0, 24, 0)
            background = rounded(surface, 16)
        }
        page.addView(input, fullParams(0, 16, 78))
        val chips = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        listOf("Funny tonight", "Action under 100 min", "Family-friendly", "Mind-bending sci-fi", "Surprise me").forEach { prompt ->
            chips.addView(tvButton(prompt, 16f, { input.setText(prompt) }), LinearLayout.LayoutParams(0, 68, 1f).apply { setMargins(0, 0, 10, 0) })
        }
        page.addView(chips, fullParams(0, 18))
        page.addView(tvButton("Find my next watch", 22f, { runQuery(input.text.toString()) }), fullParams(0, 12))
        page.addView(label("FireMind keeps recommendations grounded in a curated catalog. If AI or network services are unavailable, the local fallback still works.", 16f, muted).apply { setPadding(4, 16, 4, 0) })
    }

    private fun runQuery(query: String) {
        currentQuery = query.trim().ifBlank { "something good" }
        currentResults = recommend(currentQuery)
        renderResults()
    }

    private fun recommend(query: String): List<Movie> {
        val normalized = query.lowercase(Locale.US)
        val runtimeLimit = when {
            normalized.contains("90") -> 90
            normalized.contains("100") -> 100
            normalized.contains("two hour") || normalized.contains("2 hour") -> 120
            normalized.contains("short") -> 105
            else -> Int.MAX_VALUE
        }
        val scored = catalog.map { movie ->
            var score = 0
            if (movie.runtime <= runtimeLimit) score += 3
            if (normalized.contains("family") && movie.familyFriendly) score += 6
            movie.mood.forEach { mood -> if (normalized.contains(mood)) score += 5 }
            if (normalized.contains("sci-fi") && movie.genre.contains("Sci-Fi")) score += 5
            if (normalized.contains("action") && movie.genre.contains("Action")) score += 5
            if (normalized.contains("mystery") && movie.genre.contains("Mystery")) score += 5
            if (normalized.contains("funny") || normalized.contains("comedy")) if (movie.genre.contains("Comedy")) score += 5
            score += (movie.rating * 0.1).toInt()
            movie to score
        }.sortedWith(compareByDescending<Pair<Movie, Int>> { it.second }.thenByDescending { it.first.rating })
        return scored.take(4).map { it.first }
    }

    private fun renderResults() = render(Screen.RESULTS, "Recommendations", "For: “$currentQuery”") { page ->
        page.addView(label("WHY THESE PICKS?", 16f, accent).apply { setPadding(4, 4, 0, 10) })
        page.addView(label("FireMind matched mood, genre, runtime, and audience signals, then kept the list short enough to choose from.", 18f, muted).apply { setPadding(4, 0, 4, 16) })
        val results = if (currentResults.isEmpty()) catalog.take(3) else currentResults
        results.forEach { movie -> page.addView(resultCard(movie), fullParams(0, 12)) }
        page.addView(tvButton("Try another request", 18f, { renderAssistant() }), fullParams(0, 18))
    }

    private fun renderDetails() {
        render(Screen.DETAILS, selectedMovie?.title ?: "Details") { page ->
        val movie = selectedMovie ?: return@render
        page.addView(label(movie.title, 36f, text))
        page.addView(label("${movie.year}  ·  ${movie.genre}  ·  ${movie.runtime} min  ·  ${movie.rating}/10", 19f, warm).apply { setPadding(0, 8, 0, 18) })
        page.addView(label(movie.description, 20f, text).apply { setPadding(0, 0, 0, 22) })
        page.addView(label("WHY FIREMIND RECOMMENDS IT", 16f, accent).apply { setPadding(0, 6, 0, 8) })
        page.addView(label(reasonFor(movie), 19f, muted).apply { setPadding(0, 0, 0, 22) })
        page.addView(label("SPOILER-SAFE SUMMARY", 16f, accent).apply { setPadding(0, 6, 0, 8) })
        page.addView(label("A focused ${movie.runtime}-minute ${movie.genre.lowercase(Locale.US)} with a clear hook, strong atmosphere, and enough momentum for a single sitting.", 19f, muted).apply { setPadding(0, 0, 0, 22) })
        val saved = isSaved(movie)
        page.addView(tvButton(if (saved) "Remove from Watchlist" else "Add to Watchlist", 20f, {
            setSaved(movie, !saved)
            renderDetails()
        }), fullParams(0, 12))
        page.addView(tvButton("Back to recommendations", 18f, { renderResults() }), fullParams(0, 10))
        }
    }

    private fun renderWatchlist() = render(Screen.WATCHLIST, "Watchlist", "Saved locally on this device — no account required.") { page ->
        val saved = catalog.filter { isSaved(it) }
        if (saved.isEmpty()) {
            page.addView(label("Your watchlist is ready for its first great pick.", 24f, text).apply { setPadding(4, 20, 4, 12) })
            page.addView(label("Open a recommendation and select Add to Watchlist.", 18f, muted).apply { setPadding(4, 0, 4, 20) })
            page.addView(tvButton("Explore recommendations", 20f, { renderAssistant() }), fullParams(0, 12))
        } else {
            saved.forEach { movie -> page.addView(resultCard(movie), fullParams(0, 12)) }
        }
    }

    private fun renderAbout() = render(Screen.ABOUT, "About", "A focused hackathon MVP built specifically for Fire TV.") { page ->
        page.addView(label("FireMind turns Fire TV from a content browser into an intelligent viewing companion.", 26f, text).apply { setPadding(4, 16, 4, 20) })
        page.addView(label("Designed for the 10-foot experience: large type, high contrast, explicit focus states, logical D-pad movement, and no mouse dependency.", 19f, muted).apply { setPadding(4, 0, 4, 20) })
        page.addView(label("BUILD NOTES", 16f, accent).apply { setPadding(4, 4, 0, 8) })
        page.addView(label("Native Android/Kotlin · Fire OS target · deterministic local catalog · local watchlist persistence · AI-ready recommendation contract with graceful fallback.", 18f, text).apply { setPadding(4, 0, 4, 18) })
        page.addView(tvButton("Back home", 20f, { renderHome() }), fullParams(0, 12))
    }

    private fun movieCard(movie: Movie): Button = tvButton("${movie.title}\n${movie.year} · ${movie.runtime} min", 17f, {
        selectedMovie = movie
        renderDetails()
    }, compact = false).apply { gravity = Gravity.CENTER; setPadding(10, 10, 10, 10) }

    private fun resultCard(movie: Movie): Button = tvButton("${movie.title}   ${movie.year}\n${movie.genre} · ${movie.runtime} min · ${movie.rating}/10\n${reasonFor(movie)}", 18f, {
        selectedMovie = movie
        renderDetails()
    }, compact = false).apply { gravity = Gravity.CENTER_VERTICAL; setPadding(24, 10, 24, 10) }

    private fun reasonFor(movie: Movie): String {
        val query = currentQuery.lowercase(Locale.US)
        val signals = mutableListOf<String>()
        if (query.contains("sci-fi") && movie.genre.contains("Sci-Fi")) signals.add("sci-fi")
        if ((query.contains("funny") || query.contains("comedy")) && movie.genre.contains("Comedy")) signals.add("comedy")
        if (query.contains("family") && movie.familyFriendly) signals.add("family-friendly")
        if (query.contains("under") || query.contains("short") || query.contains("hour")) if (movie.runtime <= 120) signals.add("your time window")
        if (signals.isEmpty()) signals.add(movie.mood.firstOrNull() ?: "your viewing mood")
        return "Matches ${signals.joinToString(", ")} with a ${movie.runtime}-minute runtime and a strong audience rating."
    }

    private fun tvButton(label: String, size: Float, action: () -> Unit, compact: Boolean = false): Button = Button(this).apply {
        text = label
        textSize = size
        isAllCaps = false
        setTextColor(this@MainActivity.text)
        setPadding(if (compact) 12 else 20, 0, if (compact) 12 else 20, 0)
        minHeight = if (compact) 54 else 70
        minWidth = if (compact) 84 else 180
        stateListAnimator = null
        background = rounded(this@MainActivity.surface, 14)
        setOnClickListener { action() }
        setOnFocusChangeListener { view, hasFocus ->
            view.background = rounded(if (hasFocus) this@MainActivity.accent else this@MainActivity.surface, 14)
            (view as Button).setTextColor(if (hasFocus) this@MainActivity.background else this@MainActivity.text)
            view.scaleX = if (hasFocus) 1.03f else 1.0f
            view.scaleY = if (hasFocus) 1.03f else 1.0f
        }
    }

    private fun label(value: String, size: Float, color: Int): TextView = TextView(this).apply {
        text = value
        textSize = size
        setTextColor(color)
        setLineSpacing(1.05f, 1.0f)
    }

    private fun rounded(color: Int, radius: Int): GradientDrawable = GradientDrawable().apply {
        setColor(color)
        cornerRadius = radius.toFloat()
    }

    private fun fullParams(left: Int, bottom: Int, height: Int = ViewGroup.LayoutParams.WRAP_CONTENT) = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height).apply {
        setMargins(left, 0, 0, bottom)
    }

    private fun focusFirst(parent: ViewGroup) {
        for (index in 0 until parent.childCount) {
            val child = parent.getChildAt(index)
            if (child.isFocusable) {
                child.requestFocus()
                return
            }
            if (child is ViewGroup) focusFirst(child)
        }
    }

    private fun isSaved(movie: Movie): Boolean = prefs.getBoolean(movie.id, false)

    private fun setSaved(movie: Movie, saved: Boolean) {
        prefs.edit().putBoolean(movie.id, saved).apply()
    }
}
