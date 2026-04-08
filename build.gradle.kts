plugins {
	base
}

allprojects {
	version = System.getenv("TAG") ?: "v1.0.4-alpha"
	group = "com.github.AshleyThew"
}
