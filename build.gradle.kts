plugins {
	base
}

allprojects {
	version = System.getenv("TAG") ?: "v1.0.2-alpha"
	group = "com.github.AshleyThew"
}
