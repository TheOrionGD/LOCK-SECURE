package com.rithika.lock.data.model

object TopicData {
    val topics = listOf(
        // --- Technical Topics (10) ---
        Topic(
            id = "tech_java",
            title = "Java Programming",
            description = "OOPs concepts, Multithreading, Collections Framework, JVM Architecture & Exception Handling",
            category = InterviewCategory.TECHNICAL,
            iconName = "java",
            sampleQuestions = listOf(
                "Can you explain the main principles of OOP in Java with real-world examples?",
                "What is the difference between Method Overloading and Method Overriding in Java?",
                "How does garbage collection work in Java?",
                "Explain the internal implementation of HashMap in Java."
            )
        ),
        Topic(
            id = "tech_python",
            title = "Python Programming",
            description = "Data structures, Generators, Decorators, Lambda functions & OOPs in Python",
            category = InterviewCategory.TECHNICAL,
            iconName = "python",
            sampleQuestions = listOf(
                "What is the difference between List, Tuple, and Set in Python?",
                "Explain Python Decorators and how you would write a custom decorator.",
                "What is the Global Interpreter Lock (GIL) in Python?",
                "How does memory management work in Python?"
            )
        ),
        Topic(
            id = "tech_cpp",
            title = "C++ Programming",
            description = "Pointers, References, STL, Virtual Functions, Templates & Memory Management",
            category = InterviewCategory.TECHNICAL,
            iconName = "java",
            sampleQuestions = listOf(
                "What is the difference between a reference and a pointer in C++?",
                "Explain virtual functions and polymorphism in C++.",
                "What is the Standard Template Library (STL) and what are containers?",
                "Explain constructor overloading and copy constructors."
            )
        ),
        Topic(
            id = "tech_c",
            title = "C Programming",
            description = "Storage classes, Pointers arithmetic, Dynamic memory allocation & Preprocessors",
            category = InterviewCategory.TECHNICAL,
            iconName = "java",
            sampleQuestions = listOf(
                "What is the difference between malloc() and calloc() in C?",
                "Explain pointer arithmetic and how arrays are represented in memory.",
                "What are storage classes in C (auto, register, static, extern)?",
                "How do function pointers work in C?"
            )
        ),
        Topic(
            id = "tech_dsa",
            title = "Data Structures & Algorithms",
            description = "Arrays, Linked Lists, Trees, Graphs, Sorting algorithms & Dynamic Programming",
            category = InterviewCategory.TECHNICAL,
            iconName = "code",
            sampleQuestions = listOf(
                "What is the difference between BFS and DFS graph traversal algorithms?",
                "How do you detect a cycle in a Linked List?",
                "Explain QuickSort and its time complexity in best and worst cases.",
                "What is Dynamic Programming and when should you use memoization?"
            )
        ),
        Topic(
            id = "tech_dbms",
            title = "Database Management System (DBMS)",
            description = "SQL queries, Normalization, ACID properties, Indexing, Transactions & Joins",
            category = InterviewCategory.TECHNICAL,
            iconName = "dbms",
            sampleQuestions = listOf(
                "What are ACID properties in DBMS and why are they important?",
                "Explain the difference between WHERE and HAVING clauses in SQL.",
                "What is Database Normalization? Explain 1NF, 2NF, and 3NF.",
                "What is the difference between Primary Key, Foreign Key, and Unique Key?"
            )
        ),
        Topic(
            id = "tech_os",
            title = "Operating Systems (OS)",
            description = "Process management, Deadlocks, Virtual Memory, CPU Scheduling & Synchronization",
            category = InterviewCategory.TECHNICAL,
            iconName = "os",
            sampleQuestions = listOf(
                "What is the difference between a Process and a Thread?",
                "What is a Deadlock and what are the four necessary conditions for it?",
                "Explain Virtual Memory and Paging in Operating Systems.",
                "What are CPU scheduling algorithms? Compare FCFS and Round Robin."
            )
        ),
        Topic(
            id = "tech_cn",
            title = "Computer Networks (CN)",
            description = "OSI Model, TCP/IP, HTTP/HTTPS, IP Addressing, DNS & Network Protocols",
            category = InterviewCategory.TECHNICAL,
            iconName = "cn",
            sampleQuestions = listOf(
                "Explain the 7 layers of the OSI Model and their primary functions.",
                "What is the difference between TCP and UDP protocols?",
                "How does DNS resolution work when you type a web URL in a browser?",
                "What is the difference between HTTP and HTTPS?"
            )
        ),
        Topic(
            id = "tech_sql",
            title = "SQL & Relational Databases",
            description = "Complex Joins, Subqueries, Stored Procedures, Views & Database Indexing",
            category = InterviewCategory.TECHNICAL,
            iconName = "dbms",
            sampleQuestions = listOf(
                "Explain the difference between INNER JOIN, LEFT JOIN, and FULL OUTER JOIN.",
                "What are database indexes and how do they speed up query execution?",
                "What is a Stored Procedure and what are its advantages?",
                "How do you write a SQL query to find the second highest salary from an employee table?"
            )
        ),
        Topic(
            id = "tech_web",
            title = "Web Development & JavaScript",
            description = "HTML5, CSS3, ES6 JavaScript, DOM manipulation, Closures & Async/Await",
            category = InterviewCategory.TECHNICAL,
            iconName = "code",
            sampleQuestions = listOf(
                "What is a Closure in JavaScript and how is it used?",
                "Explain the difference between var, let, and const in ES6.",
                "How do Promises and async/await handle asynchronous operations?",
                "What is the Event Loop in JavaScript?"
            )
        ),

        // --- Non-Technical & Aptitude Topics (10) ---
        Topic(
            id = "nontech_quant",
            title = "Quantitative Aptitude",
            description = "Percentages, Profit & Loss, Time & Work, Speed & Distance, Ratio & Proportion",
            category = InterviewCategory.NON_TECHNICAL,
            iconName = "self_intro",
            sampleQuestions = listOf(
                "How would you approach solving a complex time and work problem in an aptitude test?",
                "Explain how to calculate compound interest vs simple interest.",
                "What shortcuts do you use for percentage and ratio problems?"
            )
        ),
        Topic(
            id = "nontech_logical",
            title = "Logical Reasoning",
            description = "Blood relations, Coding-decoding, Syllogisms, Number series & Seating arrangements",
            category = InterviewCategory.NON_TECHNICAL,
            iconName = "self_intro",
            sampleQuestions = listOf(
                "How do you approach circular seating arrangement puzzles?",
                "Explain how you solve syllogism questions using Venn diagrams.",
                "What strategy do you use for finding missing numbers in complex series?"
            )
        ),
        Topic(
            id = "nontech_verbal",
            title = "Verbal Ability & English",
            description = "Reading comprehension, Spotting errors, Synonyms/Antonyms & Para jumbles",
            category = InterviewCategory.NON_TECHNICAL,
            iconName = "self_intro",
            sampleQuestions = listOf(
                "How do you tackle reading comprehension passages efficiently?",
                "What are common grammatical error spotting rules in placement exams?",
                "How do you arrange para-jumble sentences in logical order?"
            )
        ),
        Topic(
            id = "nontech_self_intro",
            title = "Self Introduction",
            description = "Crafting a compelling 2-minute elevator pitch highlighting background, projects & career goals",
            category = InterviewCategory.NON_TECHNICAL,
            iconName = "self_intro",
            sampleQuestions = listOf(
                "Please introduce yourself and highlight your academic background and key projects.",
                "Walk me through your resume and why you selected your branch of study.",
                "What are your top 3 strengths and how do they make you suitable for this role?"
            )
        ),
        Topic(
            id = "nontech_hr",
            title = "HR Questions",
            description = "Strengths, weaknesses, career goals, salary expectations & relocation preference",
            category = InterviewCategory.NON_TECHNICAL,
            iconName = "hr",
            sampleQuestions = listOf(
                "Why do you want to join our company?",
                "Where do you see yourself in 5 years?",
                "What is your biggest weakness and how are you actively working to overcome it?",
                "Why should we hire you over other candidate applicants?"
            )
        ),
        Topic(
            id = "nontech_leadership",
            title = "Leadership & Initiative",
            description = "Leading campus projects, event management, problem solving & decision making",
            category = InterviewCategory.NON_TECHNICAL,
            iconName = "leadership",
            sampleQuestions = listOf(
                "Describe a time when you took initiative to lead a project or team event.",
                "How do you handle a team member who is not contributing properly?",
                "Tell me about a difficult decision you had to make in a academic project."
            )
        ),
        Topic(
            id = "nontech_teamwork",
            title = "Teamwork & Collaboration",
            description = "Handling conflict, group projects, active listening & peer collaboration",
            category = InterviewCategory.NON_TECHNICAL,
            iconName = "teamwork",
            sampleQuestions = listOf(
                "Describe a situation where you had a disagreement with a team member. How did you resolve it?",
                "How do you ensure effective communication in a remote or group team project?",
                "Give an example of a successful group project you worked on and your specific role in it."
            )
        ),
        Topic(
            id = "nontech_comm",
            title = "Communication Skills",
            description = "Expressing complex ideas clearly, active listening, articulation & situational responses",
            category = InterviewCategory.NON_TECHNICAL,
            iconName = "comm",
            sampleQuestions = listOf(
                "How would you explain a complex technical concept to a non-technical person?",
                "Describe a situation where miscommunication occurred. How did you fix it?",
                "How do you prepare for presenting your project to clients or senior leaders?"
            )
        ),
        Topic(
            id = "nontech_problem",
            title = "Problem Solving & Critical Thinking",
            description = "Analytical reasoning, root cause analysis, decision making under ambiguity",
            category = InterviewCategory.NON_TECHNICAL,
            iconName = "hr",
            sampleQuestions = listOf(
                "Describe a complex problem you faced in a project and how you analyzed its root cause.",
                "How do you make critical decisions when you have incomplete information?",
                "Give an example of how you approach debugging a difficult issue."
            )
        ),
        Topic(
            id = "nontech_etiquette",
            title = "Corporate Etiquette & Behavior",
            description = "Professionalism, workplace ethics, punctuality, adaptability & cross-cultural teamwork",
            category = InterviewCategory.NON_TECHNICAL,
            iconName = "comm",
            sampleQuestions = listOf(
                "What does professional ethics mean to you in a corporate work environment?",
                "How do you adapt when sudden changes happen in project requirements?",
                "How do you handle constructive criticism from managers or senior colleagues?"
            )
        )
    )

    fun getTopicById(id: String): Topic {
        return topics.find { it.id == id } ?: topics.first()
    }

    fun getTopicsByCategory(category: InterviewCategory): List<Topic> {
        return topics.filter { it.category == category }
    }
}
