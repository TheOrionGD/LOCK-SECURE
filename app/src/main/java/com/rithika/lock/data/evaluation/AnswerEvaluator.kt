package com.rithika.lock.data.evaluation

import com.rithika.lock.data.model.FeedbackResult
import com.rithika.lock.data.model.InterviewMessage
import com.rithika.lock.data.model.MessageSender
import com.rithika.lock.data.model.QuestionEvaluation
import java.util.Locale

/**
 * Robust Answer Evaluation and Scoring Engine for Interview Preparation.
 *
 * Scoring Rubric (Strict 0-100 range):
 * - 0–10: Incorrect or Irrelevant (greetings like "Hi"/"Hello", empty answers, nonsensical text,
 *         unrelated topics, technically incorrect statements, or buzzwords without understanding).
 * - 11–50: Partially Correct (demonstrates some genuine understanding, but missing key concepts or
 *          contains notable inaccuracies).
 * - 51–100: Correct & Sufficiently Relevant (accurately addresses the question, demonstrates meaningful
 *           technical depth; comprehensive answers receive higher scores near 100).
 */
object AnswerEvaluator {

    data class ConceptProfile(
        val questionKeywords: List<List<String>>, // At least one list must have all its tokens present
        val coreConcepts: List<String>,
        val conceptSynonyms: Map<String, List<String>> = emptyMap(),
        val factualErrors: List<String> = emptyList(),
        val idealAnswer: String
    )

    private val GREETING_TOKENS = setOf(
        "hi", "hello", "hey", "welcome", "greetings", "good morning", "good afternoon",
        "good evening", "howdy", "sup", "yo", "thanks", "thank you", "ok", "okay",
        "yes", "no", "yeah", "yep", "sure", "fine", "cool", "nice", "good",
        "understood", "alright", "sir", "madam", "ma'am", "mam", "bye", "goodbye",
        "test", "testing", "there", "how", "are", "you", "doing", "today", "iam",
        "please", "greet", "greets", "am", "i"
    )

    private val CONVERSATIONAL_PHRASES = listOf(
        "how are you", "how are you doing", "hey there", "hello there", "good morning sir",
        "good afternoon sir", "nice to meet you", "pleasure meeting you", "i am fine",
        "how do you do", "welcome sir", "hello hello", "hi there"
    )

    private val STOPWORDS = setOf(
        "a", "about", "above", "after", "again", "against", "all", "am", "an", "and", "any", "are",
        "as", "at", "be", "because", "been", "before", "being", "below", "between", "both", "but",
        "by", "can", "could", "did", "do", "does", "doing", "down", "during", "each", "few", "for",
        "from", "further", "had", "has", "have", "having", "he", "her", "here", "hers", "herself",
        "him", "himself", "his", "how", "i", "if", "in", "into", "is", "it", "its", "itself", "let",
        "me", "more", "most", "my", "myself", "no", "nor", "not", "of", "off", "on", "once", "only",
        "or", "other", "ought", "our", "ours", "ourselves", "out", "over", "own", "same", "she",
        "should", "so", "some", "such", "than", "that", "the", "their", "theirs", "them",
        "themselves", "then", "there", "these", "they", "this", "those", "through", "to", "too",
        "under", "until", "up", "very", "was", "we", "were", "what", "when", "where", "which",
        "while", "who", "whom", "why", "with", "would", "you", "your", "yours", "yourself",
        "yourselves", "explain", "describe", "difference", "between", "tell"
    )

    // Knowledge base of common interview concepts
    private val CONCEPT_PROFILES: List<ConceptProfile> = listOf(
        // Database / DBMS concepts
        ConceptProfile(
            questionKeywords = listOf(listOf("database"), listOf("dbms")),
            coreConcepts = listOf("organized collection", "store", "retrieve", "manage", "electronic"),
            conceptSynonyms = mapOf(
                "organized collection" to listOf("structured", "organized", "systematic", "repository", "records", "tables"),
                "store" to listOf("storage", "stores", "save", "persist", "persisted", "maintained", "maintain"),
                "retrieve" to listOf("access", "retrieval", "query", "fetch", "fetched", "fetching", "search", "indexed", "index"),
                "manage" to listOf("management", "manipulate", "update", "control", "dbms", "maintained", "maintain", "maintenance", "crud"),
                "electronic" to listOf("electronically", "digital", "computer", "system", "server")
            ),
            factualErrors = listOf("hardware cable", "networking switch", "router device", "compiler"),
            idealAnswer = "A database is an organized collection of structured data stored electronically in a computer system, typically controlled by a Database Management System (DBMS), enabling efficient storage, retrieval, modification, and management."
        ),
        ConceptProfile(
            questionKeywords = listOf(listOf("acid")),
            coreConcepts = listOf("atomicity", "consistency", "isolation", "durability", "transaction"),
            conceptSynonyms = mapOf(
                "atomicity" to listOf("all or nothing", "atomic"),
                "consistency" to listOf("valid state", "constraints", "consistent"),
                "isolation" to listOf("concurrency", "independent", "isolated", "isolation level"),
                "durability" to listOf("persistent", "permanence", "survives crash", "durable"),
                "transaction" to listOf("transactions", "database operation", "reliable")
            ),
            factualErrors = listOf("acid is chemical", "only atomicity matters"),
            idealAnswer = "ACID properties ensure transaction reliability: Atomicity (all-or-nothing execution), Consistency (data follows all integrity constraints), Isolation (concurrent transactions execute without interference), and Durability (committed changes persist permanently even through system failures)."
        ),
        ConceptProfile(
            questionKeywords = listOf(listOf("normalization"), listOf("normal", "form")),
            coreConcepts = listOf("redundancy", "anomalies", "1nf", "2nf", "3nf", "dependency"),
            conceptSynonyms = mapOf(
                "redundancy" to listOf("duplicate", "duplication", "minimize redundancy", "redundant"),
                "anomalies" to listOf("insertion anomaly", "deletion anomaly", "update anomaly", "data integrity"),
                "1nf" to listOf("atomic values", "first normal form", "no repeating groups", "atomic"),
                "2nf" to listOf("second normal form", "partial dependency"),
                "3nf" to listOf("third normal form", "transitive dependency")
            ),
            idealAnswer = "Database normalization structures tables to minimize redundancy and eliminate insertion, update, and deletion anomalies. 1NF enforces atomic column values, 2NF removes partial functional dependencies on composite keys, and 3NF removes transitive dependencies."
        ),
        ConceptProfile(
            questionKeywords = listOf(listOf("primary", "key"), listOf("foreign", "key"), listOf("unique", "key")),
            coreConcepts = listOf("uniquely identify", "not null", "relationship", "referential", "unique values"),
            conceptSynonyms = mapOf(
                "uniquely identify" to listOf("unique record", "identifier", "primary key"),
                "not null" to listOf("no nulls", "cannot be null"),
                "relationship" to listOf("foreign key", "references", "parent child"),
                "referential" to listOf("referential integrity", "foreign key constraint"),
                "unique values" to listOf("unique key", "allows one null", "uniqueness")
            ),
            factualErrors = listOf("primary key allows null", "foreign key cannot have null"),
            idealAnswer = "A Primary Key uniquely identifies each record in a table and cannot contain NULL values. A Unique Key enforces unique values across records but may permit NULL values. A Foreign Key references a primary or unique key in another table to establish relationships and maintain referential integrity."
        ),
        ConceptProfile(
            questionKeywords = listOf(listOf("where", "having")),
            coreConcepts = listOf("row", "group", "aggregate", "before", "after"),
            conceptSynonyms = mapOf(
                "row" to listOf("individual rows", "record level", "where filters rows", "rows"),
                "group" to listOf("grouped", "group by", "having filters groups", "groups"),
                "aggregate" to listOf("count", "sum", "avg", "aggregate functions", "aggregation")
            ),
            idealAnswer = "WHERE filters individual rows before groupings and aggregate operations are calculated. HAVING filters summarized groups after the GROUP BY clause and aggregate calculations (such as COUNT, SUM, AVG) have been applied."
        ),

        // Java concepts
        ConceptProfile(
            questionKeywords = listOf(listOf("oop"), listOf("object", "oriented")),
            coreConcepts = listOf("encapsulation", "inheritance", "polymorphism", "abstraction"),
            conceptSynonyms = mapOf(
                "encapsulation" to listOf("data hiding", "private fields", "getters", "setters", "encapsulate"),
                "inheritance" to listOf("extends", "subclass", "superclass", "reusability", "derived", "inherit"),
                "polymorphism" to listOf("overloading", "overriding", "many forms", "dynamic dispatch", "polymorphic"),
                "abstraction" to listOf("interfaces", "abstract class", "hide implementation", "hiding details", "abstract")
            ),
            idealAnswer = "Object-Oriented Programming (OOP) in Java relies on four fundamental principles: Encapsulation (bundling data and restricting direct access via access modifiers), Inheritance (deriving new classes from existing classes for code reuse), Polymorphism (executing a single action in different ways via overriding and overloading), and Abstraction (hiding implementation complexity using interfaces and abstract classes)."
        ),
        ConceptProfile(
            questionKeywords = listOf(listOf("overloading"), listOf("overriding")),
            coreConcepts = listOf("same class", "subclass", "parameters", "compile time", "runtime"),
            conceptSynonyms = mapOf(
                "same class" to listOf("within class", "single class", "overloading in same class"),
                "subclass" to listOf("inheritance", "parent child", "derived class", "overriding in subclass"),
                "parameters" to listOf("different arguments", "signature", "parameter list", "parameter"),
                "compile time" to listOf("static polymorphism", "early binding"),
                "runtime" to listOf("dynamic polymorphism", "late binding", "dynamic dispatch")
            ),
            idealAnswer = "Method Overloading happens within the same class where multiple methods have the same name but different parameter lists, resolved at compile-time. Method Overriding occurs when a subclass re-implements a superclass method with the identical signature and return type, resolved dynamically at runtime."
        ),
        ConceptProfile(
            questionKeywords = listOf(listOf("garbage", "collection"), listOf("gc")),
            coreConcepts = listOf("automatic", "memory", "heap", "unreachable", "mark and sweep"),
            conceptSynonyms = mapOf(
                "automatic" to listOf("automated", "jvm background", "deallocation"),
                "memory" to listOf("ram", "reclaim", "free memory"),
                "heap" to listOf("heap memory", "objects"),
                "unreachable" to listOf("unreferenced", "no reference", "dereferenced"),
                "mark and sweep" to listOf("gc algorithm", "collector", "g1")
            ),
            idealAnswer = "Garbage collection in Java is an automatic memory management process in the JVM that identifies and reclaims heap memory occupied by unreachable or unreferenced objects, preventing memory leaks using algorithms like Mark-and-Sweep."
        ),
        ConceptProfile(
            questionKeywords = listOf(listOf("hashmap")),
            coreConcepts = listOf("hashing", "bucket", "hashcode", "equals", "linked list", "red black tree"),
            conceptSynonyms = mapOf(
                "hashing" to listOf("hash function", "index calculation"),
                "bucket" to listOf("array of nodes", "entry array", "slots", "buckets"),
                "hashcode" to listOf("hash code", "hash value"),
                "equals" to listOf("equals method", "key comparison"),
                "linked list" to listOf("chaining", "separate chaining"),
                "red black tree" to listOf("treeify", "binary tree", "threshold 8", "tree")
            ),
            idealAnswer = "HashMap internally uses an array of Node buckets. A key's hashCode() determines its bucket index; collisions are handled by chaining elements in a linked list, which upgrades to a self-balancing Red-Black Tree once bucket depth reaches 8 for efficient O(log n) lookups."
        ),
        ConceptProfile(
            questionKeywords = listOf(listOf("stringbuilder"), listOf("stringbuffer"), listOf("string", "mutable")),
            coreConcepts = listOf("immutable", "mutable", "thread safe", "synchronized", "performance"),
            conceptSynonyms = mapOf(
                "immutable" to listOf("cannot change", "string pool", "unmodifiable", "immutability"),
                "mutable" to listOf("modifiable", "append in place", "mutability"),
                "thread safe" to listOf("thread safety", "threadsafe"),
                "synchronized" to listOf("synchronization", "thread locking"),
                "performance" to listOf("faster", "efficiency", "single thread")
            ),
            factualErrors = listOf("string is mutable", "stringbuilder is thread safe", "stringbuilder is synchronized"),
            idealAnswer = "String is immutable; any modification creates a new object in memory. StringBuilder is mutable and not thread-safe, offering maximum speed for single-threaded operations. StringBuffer is mutable and thread-safe because its methods are synchronized."
        ),

        // Operating Systems concepts
        ConceptProfile(
            questionKeywords = listOf(listOf("process", "thread")),
            coreConcepts = listOf("independent", "memory", "address space", "lightweight", "share", "context switch"),
            conceptSynonyms = mapOf(
                "independent" to listOf("separate", "isolated", "program in execution"),
                "memory" to listOf("address space", "dedicated memory"),
                "lightweight" to listOf("lightweight process", "unit of execution"),
                "share" to listOf("shared memory", "shares heap", "same address space", "shares"),
                "context switch" to listOf("context switching", "overhead", "switching cost")
            ),
            idealAnswer = "A Process is an independent program in execution with its own dedicated memory address space and system resources. A Thread is a lightweight unit of execution within a process that shares memory and resources with sibling threads, resulting in much faster context switching and lower overhead."
        ),
        ConceptProfile(
            questionKeywords = listOf(listOf("deadlock")),
            coreConcepts = listOf("mutual exclusion", "hold and wait", "no preemption", "circular wait", "blocked"),
            conceptSynonyms = mapOf(
                "mutual exclusion" to listOf("exclusive access", "one process at a time"),
                "hold and wait" to listOf("holding resources", "waiting for other"),
                "no preemption" to listOf("cannot be taken away", "non preemptive"),
                "circular wait" to listOf("cycle of dependencies", "circular dependency"),
                "blocked" to listOf("waiting permanently", "starvation", "freeze")
            ),
            idealAnswer = "A Deadlock is an operating system state where a set of processes are permanently blocked because each holds a resource while waiting for another held by a different process. The four necessary conditions are Mutual Exclusion, Hold and Wait, No Preemption, and Circular Wait."
        ),
        ConceptProfile(
            questionKeywords = listOf(listOf("virtual", "memory"), listOf("paging")),
            coreConcepts = listOf("ram", "secondary storage", "pages", "frames", "page fault", "address translation"),
            conceptSynonyms = mapOf(
                "ram" to listOf("physical memory", "main memory"),
                "secondary storage" to listOf("disk", "swap space", "hard drive"),
                "pages" to listOf("virtual pages", "fixed size blocks"),
                "frames" to listOf("physical frames", "page frames"),
                "page fault" to listOf("fault interrupt", "swapping", "page miss")
            ),
            idealAnswer = "Virtual Memory allows execution of processes larger than physical RAM by mapping virtual addresses to secondary storage. Paging divides virtual memory into fixed-size pages and physical memory into frames; when an unmapped page is accessed, a page fault interrupts execution to swap it into RAM."
        ),

        // Computer Networks concepts
        ConceptProfile(
            questionKeywords = listOf(listOf("tcp"), listOf("udp")),
            coreConcepts = listOf("connection oriented", "connectionless", "reliable", "unreliable", "handshake", "ordering"),
            conceptSynonyms = mapOf(
                "connection oriented" to listOf("established connection", "handshake"),
                "connectionless" to listOf("no connection", "datagram"),
                "reliable" to listOf("acknowledgment", "error checking", "guaranteed delivery"),
                "unreliable" to listOf("no acknowledgment", "best effort delivery"),
                "handshake" to listOf("3 way handshake", "three way handshake"),
                "ordering" to listOf("packet sequencing", "ordered delivery")
            ),
            factualErrors = listOf("tcp is connectionless", "udp is reliable", "udp guarantees delivery"),
            idealAnswer = "TCP is a connection-oriented, reliable protocol that guarantees in-order packet delivery using acknowledgments and a 3-way handshake, used for web and file transfers. UDP is a connectionless, lightweight protocol with no delivery or ordering guarantees, optimal for low-latency streaming and gaming."
        ),
        ConceptProfile(
            questionKeywords = listOf(listOf("osi")),
            coreConcepts = listOf("physical", "data link", "network", "transport", "session", "presentation", "application"),
            conceptSynonyms = mapOf(
                "physical" to listOf("layer 1", "bits", "cables"),
                "data link" to listOf("layer 2", "mac", "frames"),
                "network" to listOf("layer 3", "ip", "routing", "packets"),
                "transport" to listOf("layer 4", "tcp", "udp", "ports", "segments"),
                "application" to listOf("layer 7", "http", "dns", "ftp")
            ),
            idealAnswer = "The OSI model features 7 architectural layers: Physical, Data Link, Network, Transport, Session, Presentation, and Application. The Transport Layer governs end-to-end communication, segmentation, flow control, error recovery, and multiplexing via port numbers."
        ),
        ConceptProfile(
            questionKeywords = listOf(listOf("http"), listOf("https")),
            coreConcepts = listOf("plaintext", "encrypted", "port 80", "port 443", "ssl", "tls", "security"),
            conceptSynonyms = mapOf(
                "plaintext" to listOf("unencrypted", "clear text"),
                "encrypted" to listOf("encryption", "ciphertext", "secure communication"),
                "ssl" to listOf("tls", "certificate", "digital certificate"),
                "port 80" to listOf("80"),
                "port 443" to listOf("443")
            ),
            factualErrors = listOf("http is secure", "https uses port 80", "http uses ssl"),
            idealAnswer = "HTTP transfers data as unencrypted plain text over port 80, leaving it susceptible to interception. HTTPS encrypts traffic over port 443 using SSL/TLS protocols, ensuring data confidentiality, integrity, and server authentication via digital certificates."
        ),

        // Python concepts
        ConceptProfile(
            questionKeywords = listOf(listOf("mutable"), listOf("immutable")),
            coreConcepts = listOf("modified", "unchangeable", "list", "tuple", "memory", "id"),
            conceptSynonyms = mapOf(
                "modified" to listOf("change in place", "mutate", "mutable"),
                "unchangeable" to listOf("cannot change", "immutable", "new object"),
                "list" to listOf("dict", "set", "dictionary"),
                "tuple" to listOf("int", "str", "string", "float")
            ),
            idealAnswer = "In Python, mutable objects (like lists, dictionaries, and sets) can be altered in-place after creation without changing their memory ID. Immutable objects (like integers, strings, and tuples) cannot be modified once instantiated; any modification produces a distinct new object."
        ),
        ConceptProfile(
            questionKeywords = listOf(listOf("decorator"), listOf("decorators")),
            coreConcepts = listOf("higher order function", "wrapper", "arguments", "modify behavior", "without changing"),
            conceptSynonyms = mapOf(
                "higher order function" to listOf("takes function", "returns function"),
                "wrapper" to listOf("inner function", "wrapper function"),
                "modify behavior" to listOf("extend behavior", "adds functionality"),
                "without changing" to listOf("without modifying source", "clean code", "@ syntax")
            ),
            idealAnswer = "A decorator in Python is a higher-order function that takes another function as an argument, extends or alters its behavior through an inner wrapper function, and returns the modified function without altering its original source code."
        ),
        ConceptProfile(
            questionKeywords = listOf(listOf("gil"), listOf("global", "interpreter", "lock")),
            coreConcepts = listOf("mutex", "cpython", "single thread", "bytecode", "multithreading", "parallel"),
            conceptSynonyms = mapOf(
                "mutex" to listOf("lock", "synchronization lock"),
                "cpython" to listOf("standard python", "python interpreter"),
                "single thread" to listOf("one thread at a time", "single cpu core"),
                "bytecode" to listOf("executes bytecode", "instruction"),
                "parallel" to listOf("multiprocessing", "cpu bound")
            ),
            idealAnswer = "The Global Interpreter Lock (GIL) is a mutex in CPython that restricts execution of Python bytecode to a single thread at any given moment, safeguarding internal memory management but restricting CPU-bound multithreaded parallelism."
        ),

        // HR / General questions
        ConceptProfile(
            questionKeywords = listOf(listOf("introduce"), listOf("introduction")),
            coreConcepts = listOf("background", "education", "projects", "skills", "career"),
            conceptSynonyms = mapOf(
                "background" to listOf("degree", "engineering", "college", "academic"),
                "projects" to listOf("built", "application", "major project", "technologies"),
                "skills" to listOf("programming", "strengths", "languages", "tools"),
                "career" to listOf("aspiration", "goal", "passion", "placement")
            ),
            idealAnswer = "A compelling self-introduction should articulate your academic background, core technical proficiencies, major projects engineered with real-world impact, and your forward-looking career motivation."
        ),
        ConceptProfile(
            questionKeywords = listOf(listOf("why", "join"), listOf("why", "hire"), listOf("choose", "you")),
            coreConcepts = listOf("skills", "values", "growth", "contribute", "problem solving"),
            conceptSynonyms = mapOf(
                "skills" to listOf("technical ability", "qualifications", "competence"),
                "values" to listOf("company culture", "mission", "vision"),
                "growth" to listOf("learning", "career development", "opportunities"),
                "contribute" to listOf("solve problems", "value add", "deliver results")
            ),
            idealAnswer = "Structure your answer by aligning your technical strengths and problem-solving drive directly with the company's projects, showcasing cultural enthusiasm and eagerness to deliver immediate value."
        )
    )

    data class EvaluationResult(
        val score: Int,
        val status: String,
        val explanation: String,
        val missingConcepts: String,
        val suggestedImprovements: String,
        val idealAnswerSample: String
    ) {
        val scoreOutOfTen: Int get() = (score / 10).coerceIn(0, 10)

        fun formattedFeedback(): String {
            return buildString {
                append("Status: $status. ")
                append(explanation)
                if (missingConcepts.isNotBlank()) {
                    append(" Missing concepts: $missingConcepts.")
                }
                if (suggestedImprovements.isNotBlank()) {
                    append(" Suggested improvements: $suggestedImprovements.")
                }
            }.trim()
        }
    }

    /**
     * Evaluates a single candidate answer against a question text and optional topic.
     */
    fun evaluateAnswer(
        questionText: String,
        candidateAnswer: String,
        topicTitle: String = ""
    ): EvaluationResult {
        val rawAnswer = candidateAnswer.trim()

        // 1. Check for empty or whitespace-only answers
        if (rawAnswer.isBlank()) {
            val matchingProfile = findMatchingProfile(questionText)
            val ideal = matchingProfile?.idealAnswer
                ?: generateGenericIdealAnswer(questionText)
            return EvaluationResult(
                score = 0,
                status = "Incorrect",
                explanation = "No answer was provided. An empty response cannot be evaluated.",
                missingConcepts = "All technical concepts addressing the question.",
                suggestedImprovements = "Attempt every question by directly defining terms and providing practical technical examples.",
                idealAnswerSample = ideal
            )
        }

        // 2. Check for Greetings and conversational pleasantries (e.g., "Hi", "Hello", "Welcome")
        if (isGreetingOnly(rawAnswer)) {
            val matchingProfile = findMatchingProfile(questionText)
            val ideal = matchingProfile?.idealAnswer
                ?: generateGenericIdealAnswer(questionText)
            return EvaluationResult(
                score = 0,
                status = "Incorrect",
                explanation = "\"$rawAnswer\" is a greeting or conversational phrase and does not explain or address the question asked. The answer contains no relevant technical information.",
                missingConcepts = "Core definitions, technical principles, and explanations required by the question.",
                suggestedImprovements = "Respond directly with the technical concepts and explanations asked in the interview question rather than conversational greetings.",
                idealAnswerSample = ideal
            )
        }

        // 3. Check for Gibberish or random character smashes
        if (isGibberish(rawAnswer)) {
            val matchingProfile = findMatchingProfile(questionText)
            val ideal = matchingProfile?.idealAnswer
                ?: generateGenericIdealAnswer(questionText)
            return EvaluationResult(
                score = 0,
                status = "Incorrect",
                explanation = "The response appears to be random or nonsensical text. It contains no valid technical information or meaningful explanation.",
                missingConcepts = "Factual concepts and coherent technical explanations.",
                suggestedImprovements = "Provide a structured, coherent answer using standard technical terminology.",
                idealAnswerSample = ideal
            )
        }

        // 4. Match against Known Concept Profiles
        val profile = findMatchingProfile(questionText)
        if (profile != null) {
            return evaluateWithProfile(questionText, rawAnswer, profile)
        }

        // 5. Fallback Generic Semantic Evaluator for arbitrary or dynamic questions
        return evaluateGenericQuestion(questionText, rawAnswer, topicTitle)
    }

    private fun evaluateWithProfile(
        questionText: String,
        answer: String,
        profile: ConceptProfile
    ): EvaluationResult {
        val normalizedAnswer = answer.lowercase(Locale.ROOT)

        // Check for severe factual errors
        for (err in profile.factualErrors) {
            if (normalizedAnswer.contains(err.lowercase(Locale.ROOT))) {
                return EvaluationResult(
                    score = 10,
                    status = "Incorrect",
                    explanation = "The answer contains a critical factual error regarding '$err'. This contradicts established technical principles.",
                    missingConcepts = profile.coreConcepts.joinToString(", "),
                    suggestedImprovements = "Review the fundamental theory: ${profile.idealAnswer}",
                    idealAnswerSample = profile.idealAnswer
                )
            }
        }

        // Count matched core concepts
        val matchedConcepts = mutableListOf<String>()
        val missingConcepts = mutableListOf<String>()

        for (concept in profile.coreConcepts) {
            val synonyms = profile.conceptSynonyms[concept] ?: listOf(concept)
            val allForms = listOf(concept) + synonyms
            val found = allForms.any { form ->
                containsWordOrPhrase(normalizedAnswer, form.lowercase(Locale.ROOT))
            }
            if (found) {
                matchedConcepts.add(concept)
            } else {
                missingConcepts.add(concept)
            }
        }

        // Semantic relevance check: If NO core concepts or synonyms matched at all
        if (matchedConcepts.isEmpty()) {
            val qTokens = extractSignificantTokens(questionText)
            val overlap = qTokens.filter { containsWordOrPhrase(normalizedAnswer, it) }

            if (overlap.isEmpty() && answer.length < 50) {
                return EvaluationResult(
                    score = 0,
                    status = "Incorrect",
                    explanation = "The answer is completely unrelated to the question asked. It demonstrates no relevant understanding of the subject.",
                    missingConcepts = profile.coreConcepts.joinToString(", "),
                    suggestedImprovements = "Address the core inquiry directly by explaining the key principles.",
                    idealAnswerSample = profile.idealAnswer
                )
            } else if (overlap.isEmpty()) {
                return EvaluationResult(
                    score = 5,
                    status = "Incorrect",
                    explanation = "The explanation appears to discuss unrelated concepts. No valid technical coverage of the expected topics was detected.",
                    missingConcepts = profile.coreConcepts.joinToString(", "),
                    suggestedImprovements = "Stay focused on the asked topic: ${profile.idealAnswer}",
                    idealAnswerSample = profile.idealAnswer
                )
            } else {
                return EvaluationResult(
                    score = 10,
                    status = "Incorrect",
                    explanation = "The answer mentions keywords in passing but does not demonstrate meaningful technical understanding or explain how the concepts work.",
                    missingConcepts = profile.coreConcepts.joinToString(", "),
                    suggestedImprovements = "Explain the concepts clearly rather than just mentioning disconnected keywords.",
                    idealAnswerSample = profile.idealAnswer
                )
            }
        }

        val totalConcepts = profile.coreConcepts.size.coerceAtLeast(1)
        val matchRatio = matchedConcepts.size.toDouble() / totalConcepts.toDouble()

        // Distinguish between partially correct (11-50) and correct (51-100)
        val score: Int
        val status: String
        val explanation: String

        if (matchRatio < 0.45) {
            // Limited relevant information -> Partially Correct (11–50)
            score = (20 + (matchRatio * 50).toInt()).coerceIn(11, 48)
            status = "Partially Correct"
            explanation = "Partially correct. You correctly identified relevant points (${matchedConcepts.joinToString(", ")}), but the response is incomplete and lacks essential depth."
        } else if (matchRatio < 0.75) {
            // Meaningful understanding -> Correct & Sufficiently Relevant (51–80)
            score = (52 + (matchRatio * 35).toInt()).coerceIn(51, 80)
            status = "Correct"
            explanation = "Good answer! You demonstrated solid understanding by explaining ${matchedConcepts.joinToString(", ")}."
        } else {
            // Comprehensive coverage -> Correct (81–100)
            score = (82 + ((matchRatio - 0.75) * 60).toInt()).coerceIn(81, 98)
            status = "Correct"
            explanation = "Excellent and comprehensive response! You accurately covered all key technical dimensions (${matchedConcepts.joinToString(", ")})."
        }

        return EvaluationResult(
            score = score,
            status = status,
            explanation = explanation,
            missingConcepts = if (missingConcepts.isNotEmpty()) missingConcepts.joinToString(", ") else "None",
            suggestedImprovements = if (missingConcepts.isNotEmpty()) "To elevate your score further, also explain: ${missingConcepts.joinToString(", ")}." else "Maintain this high standard of technical precision.",
            idealAnswerSample = profile.idealAnswer
        )
    }

    private fun evaluateGenericQuestion(
        questionText: String,
        answer: String,
        topicTitle: String
    ): EvaluationResult {
        val normalizedAnswer = answer.lowercase(Locale.ROOT)
        val qTokens = extractSignificantTokens(questionText)
        val topicTokens = extractSignificantTokens(topicTitle)

        // Find how many question tokens appear in candidate answer
        val matchedTokens = qTokens.filter { containsWordOrPhrase(normalizedAnswer, it) }
        val matchedTopic = topicTokens.filter { containsWordOrPhrase(normalizedAnswer, it) }

        val ideal = generateGenericIdealAnswer(questionText)

        // Completely unrelated text
        if (matchedTokens.isEmpty() && matchedTopic.isEmpty()) {
            return EvaluationResult(
                score = 5,
                status = "Incorrect",
                explanation = "The response does not appear relevant to the question asked. It contains no matching technical concepts from the inquiry.",
                missingConcepts = "Core principles addressing: $questionText",
                suggestedImprovements = "Focus directly on the concepts mentioned in the question.",
                idealAnswerSample = ideal
            )
        }

        val overlapRatio = if (qTokens.isNotEmpty()) matchedTokens.size.toDouble() / qTokens.size.toDouble() else 0.0

        // Keyword overlap only without depth
        if (answer.split(Regex("\\s+")).size < 8 && overlapRatio < 0.3) {
            return EvaluationResult(
                score = 10,
                status = "Incorrect",
                explanation = "The answer merely mentions a keyword without providing meaningful explanation or depth.",
                missingConcepts = "Substantive technical explanation and practical application.",
                suggestedImprovements = "Expand on what the concept is, how it functions, and why it is used.",
                idealAnswerSample = ideal
            )
        }

        // Partially correct (11–50)
        if (overlapRatio < 0.5 || answer.length < 60) {
            val calculated = (15 + (overlapRatio * 40).toInt()).coerceIn(15, 48)
            return EvaluationResult(
                score = calculated,
                status = "Partially Correct",
                explanation = "The answer demonstrates initial awareness of the question but lacks technical completeness and elaboration.",
                missingConcepts = "Detailed architecture, edge cases, or practical examples.",
                suggestedImprovements = "Add architectural details, benefits, trade-offs, or code illustrations to strengthen the answer.",
                idealAnswerSample = ideal
            )
        }

        // Correct and sufficiently relevant (51–100)
        val calculated = when {
            answer.length > 200 && overlapRatio >= 0.7 -> 90
            answer.length > 120 -> 78
            else -> 62
        }.coerceIn(51, 95)

        return EvaluationResult(
            score = calculated,
            status = "Correct",
            explanation = "Accurate and relevant explanation addressing the question effectively.",
            missingConcepts = "Advanced edge cases and enterprise trade-off considerations.",
            suggestedImprovements = "Include specific real-world implementation anecdotes or optimization strategies.",
            idealAnswerSample = ideal
        )
    }

    /**
     * Sanitizes and validates an evaluation returned from external AI (Gemini).
     * Enforces the strict scoring bounds, status consistency, and relevance safeguard.
     */
    fun sanitizeEvaluation(
        eval: QuestionEvaluation,
        topicTitle: String = ""
    ): QuestionEvaluation {
        val qText = eval.questionText
        val ans = eval.candidateAnswer.trim()

        // Safeguard 1: If answer is empty or greeting or gibberish, external AI MUST NOT award points!
        if (ans.isBlank() || isGreetingOnly(ans) || isGibberish(ans)) {
            val localEval = evaluateAnswer(qText, ans, topicTitle)
            return QuestionEvaluation(
                questionNumber = eval.questionNumber,
                questionText = qText,
                candidateAnswer = ans,
                feedback = localEval.formattedFeedback(),
                idealAnswerSample = if (eval.idealAnswerSample.isNotBlank()) eval.idealAnswerSample else localEval.idealAnswerSample,
                scoreOutOfTen = localEval.scoreOutOfTen,
                score = localEval.score,
                status = localEval.status
            )
        }

        // Safeguard 2: Check semantic relevance locally
        val localEval = evaluateAnswer(qText, ans, topicTitle)
        var validatedScore = eval.score.takeIf { it in 0..100 } ?: (eval.scoreOutOfTen * 10).coerceIn(0, 100)

        // If local evaluator determined the answer is completely irrelevant (score <= 10)
        if (localEval.score <= 10) {
            validatedScore = localEval.score
        } else if (localEval.status == "Partially Correct" && validatedScore > 50) {
            // Cap score if missing major core concepts
            validatedScore = 50
        }

        validatedScore = validatedScore.coerceIn(0, 100)

        val enforcedStatus = when {
            validatedScore >= 51 -> "Correct"
            validatedScore >= 11 -> "Partially Correct"
            else -> "Incorrect"
        }

        val feedbackText = if (localEval.score <= 10) {
            localEval.formattedFeedback()
        } else if (eval.feedback.isNotBlank()) {
            eval.feedback
        } else {
            localEval.formattedFeedback()
        }

        return QuestionEvaluation(
            questionNumber = eval.questionNumber,
            questionText = qText,
            candidateAnswer = ans,
            feedback = feedbackText,
            idealAnswerSample = eval.idealAnswerSample.ifBlank { localEval.idealAnswerSample },
            scoreOutOfTen = (validatedScore / 10).coerceIn(0, 10),
            score = validatedScore,
            status = enforcedStatus
        )
    }

    /**
     * Evaluates a full interview session locally.
     */
    fun evaluateInterview(
        topicTitle: String,
        messages: List<InterviewMessage>
    ): FeedbackResult {
        val pairs = extractQuestionAnswerPairs(messages)
        if (pairs.isEmpty()) {
            return generateEmptySessionFeedback(topicTitle)
        }

        val qEvals = pairs.mapIndexed { idx, pair ->
            val evalResult = evaluateAnswer(pair.first, pair.second, topicTitle)
            QuestionEvaluation(
                questionNumber = idx + 1,
                questionText = pair.first,
                candidateAnswer = pair.second,
                feedback = evalResult.formattedFeedback(),
                idealAnswerSample = evalResult.idealAnswerSample,
                scoreOutOfTen = evalResult.scoreOutOfTen,
                score = evalResult.score,
                status = evalResult.status
            )
        }

        val avgScore = qEvals.map { it.score }.average().toInt().coerceIn(0, 100)
        val technicalScore = avgScore
        val answerQualityScore = avgScore
        val communicationScore = if (avgScore == 0) 10 else (avgScore + 5).coerceIn(10, 95)
        val confidenceScore = if (avgScore == 0) 10 else avgScore.coerceIn(10, 95)

        val strengths = mutableListOf<String>()
        val weakAreas = mutableListOf<String>()
        val suggestions = mutableListOf<String>()

        if (avgScore >= 70) {
            strengths.add("Strong conceptual understanding of $topicTitle fundamentals")
            strengths.add("Clear and structured technical explanations")
            weakAreas.add("Explore more advanced edge cases and production scalability")
            suggestions.add("Practice framing answers with the STAR method for behavioral and scenario questions")
        } else if (avgScore >= 40) {
            strengths.add("Basic familiarity with foundational terminology")
            weakAreas.add("Incomplete explanations missing core technical principles")
            weakAreas.add("Needs deeper understanding of internal mechanics and trade-offs")
            suggestions.add("Review core technical definitions and practice answering with concrete examples")
            suggestions.add("Elaborate beyond brief one-line responses to demonstrate comprehensive knowledge")
        } else {
            weakAreas.add("Answers did not adequately address the technical questions asked")
            weakAreas.add("Submitted greetings, blank responses, or unrelated answers")
            suggestions.add("Study the fundamental concepts of $topicTitle thoroughly before reattempting")
            suggestions.add("Provide direct technical explanations instead of conversational greetings")
        }

        return FeedbackResult(
            overallScore = avgScore,
            technicalScore = technicalScore,
            communicationScore = communicationScore,
            confidenceScore = confidenceScore,
            answerQualityScore = answerQualityScore,
            strengths = strengths,
            weakAreas = weakAreas,
            suggestions = suggestions,
            questionEvaluations = qEvals
        )
    }

    /**
     * Sanitizes and validates the full FeedbackResult from Gemini API.
     */
    fun sanitizeFeedbackResult(
        result: FeedbackResult,
        topicTitle: String,
        messages: List<InterviewMessage>
    ): FeedbackResult {
        val sanitizedEvals = result.questionEvaluations.map { eval ->
            sanitizeEvaluation(eval, topicTitle)
        }

        val finalEvals = if (sanitizedEvals.isNotEmpty()) {
            sanitizedEvals
        } else {
            evaluateInterview(topicTitle, messages).questionEvaluations
        }

        // Validate overall scores against sanitized question evaluations
        val actualAvgScore = finalEvals.map { it.score }.average().toInt().coerceIn(0, 100)

        // If all questions scored 0-10 (e.g. all greetings), overallScore CANNOT be high
        val overall = if (actualAvgScore <= 10) actualAvgScore else result.overallScore.coerceIn(0, 100)
        val technical = if (actualAvgScore <= 10) actualAvgScore else result.technicalScore.coerceIn(0, 100)
        val answerQuality = if (actualAvgScore <= 10) actualAvgScore else result.answerQualityScore.coerceIn(0, 100)

        return result.copy(
            overallScore = overall,
            technicalScore = technical,
            communicationScore = result.communicationScore.coerceIn(0, 100),
            confidenceScore = result.confidenceScore.coerceIn(0, 100),
            answerQualityScore = answerQuality,
            questionEvaluations = finalEvals
        )
    }

    // --- Helper functions ---

    private fun isGreetingOnly(text: String): Boolean {
        val cleaned = text.lowercase(Locale.ROOT)
            .replace(Regex("[^a-z0-9\\s]"), " ")
            .trim()
        if (cleaned.isBlank()) return false

        // Check common conversational phrases
        for (phrase in CONVERSATIONAL_PHRASES) {
            if (cleaned == phrase || cleaned.startsWith("$phrase ") || cleaned.endsWith(" $phrase")) {
                val remainder = cleaned.replace(phrase, "").trim()
                if (remainder.isBlank() || remainder.split(Regex("\\s+")).all { GREETING_TOKENS.contains(it) }) {
                    return true
                }
            }
        }

        val tokens = cleaned.split(Regex("\\s+")).filter { it.isNotBlank() }
        if (tokens.isEmpty()) return false

        // Single word greeting
        if (tokens.size == 1 && GREETING_TOKENS.contains(tokens[0])) {
            return true
        }

        // All words are greetings or filler
        val allGreetings = tokens.all { GREETING_TOKENS.contains(it) }
        if (allGreetings) return true

        // Short phrases with predominantly greeting words (e.g. "hey there how are you", "hello sir good morning")
        if (tokens.size <= 8) {
            val greetingCount = tokens.count { GREETING_TOKENS.contains(it) }
            if (greetingCount >= tokens.size - 1) return true
        }

        return false
    }

    private fun isGibberish(text: String): Boolean {
        val cleaned = text.trim()
        if (cleaned.length < 4) return false

        // Repeated single character like "aaaaaa" or "hahaha"
        if (Regex("""(.)\1{4,}""").containsMatchIn(cleaned)) return true

        // Check if there are zero vowels in long alphabetic words
        val words = cleaned.lowercase(Locale.ROOT).split(Regex("\\s+"))
        for (w in words) {
            if (w.length >= 6 && w.all { it in 'a'..'z' } && !w.any { it in "aeiou" }) {
                return true
            }
        }

        // Keystroke smash patterns (e.g. "asdfghjkl", "qwertyuiop")
        val smashes = listOf("asdfgh", "qwerty", "zxcvbn", "lkjhgf", "123456")
        if (smashes.any { cleaned.lowercase(Locale.ROOT).contains(it) }) return true

        return false
    }

    private fun findMatchingProfile(questionText: String): ConceptProfile? {
        val qLower = questionText.lowercase(Locale.ROOT)
        return CONCEPT_PROFILES.firstOrNull { profile ->
            profile.questionKeywords.any { group ->
                group.all { kw -> qLower.contains(kw) }
            }
        }
    }

    private fun extractSignificantTokens(text: String): List<String> {
        val cleaned = text.lowercase(Locale.ROOT)
            .replace(Regex("[^a-z0-9\\s]"), " ")
        return cleaned.split(Regex("\\s+"))
            .filter { it.length > 2 && !STOPWORDS.contains(it) }
            .distinct()
    }

    private fun containsWordOrPhrase(text: String, wordOrPhrase: String): Boolean {
        if (wordOrPhrase.contains(" ")) {
            return text.contains(wordOrPhrase)
        }
        // Direct word boundary match
        val exactPattern = Regex("\\b${Regex.escape(wordOrPhrase)}\\b")
        if (exactPattern.containsMatchIn(text)) return true

        // Check morphological stem match (e.g. fetch -> fetched, store -> stored/storage, retrieve -> retrieved)
        if (wordOrPhrase.length >= 4) {
            val stem = when {
                wordOrPhrase.endsWith("ing") -> wordOrPhrase.removeSuffix("ing")
                wordOrPhrase.endsWith("ed") -> wordOrPhrase.removeSuffix("ed")
                wordOrPhrase.endsWith("es") -> wordOrPhrase.removeSuffix("es")
                wordOrPhrase.endsWith("s") -> wordOrPhrase.removeSuffix("s")
                else -> wordOrPhrase
            }
            if (stem.length >= 4 && text.contains(stem)) {
                return true
            }
        }
        return false
    }

    private fun generateGenericIdealAnswer(questionText: String): String {
        return "A comprehensive answer should clearly define the core concepts, describe internal mechanics and key benefits, and conclude with a practical real-world example."
    }

    private fun extractQuestionAnswerPairs(messages: List<InterviewMessage>): List<Pair<String, String>> {
        val pairs = mutableListOf<Pair<String, String>>()
        var currentQ = ""
        for (msg in messages) {
            if (msg.sender == MessageSender.INTERVIEWER) {
                currentQ = msg.text
            } else if (msg.sender == MessageSender.CANDIDATE && currentQ.isNotBlank()) {
                pairs.add(Pair(currentQ, msg.text))
                currentQ = ""
            }
        }
        return pairs
    }

    private fun generateEmptySessionFeedback(topicTitle: String): FeedbackResult {
        return FeedbackResult(
            overallScore = 0,
            technicalScore = 0,
            communicationScore = 0,
            confidenceScore = 0,
            answerQualityScore = 0,
            strengths = emptyList(),
            weakAreas = listOf("No questions were attempted during this interview session"),
            suggestions = listOf("Complete an interview by submitting substantive technical answers to each question"),
            questionEvaluations = listOf(
                QuestionEvaluation(
                    questionNumber = 1,
                    questionText = "Interview evaluation for $topicTitle",
                    candidateAnswer = "",
                    feedback = "No responses provided.",
                    idealAnswerSample = "Ensure complete coverage of fundamental principles.",
                    scoreOutOfTen = 0,
                    score = 0,
                    status = "Incorrect"
                )
            )
        )
    }
}
