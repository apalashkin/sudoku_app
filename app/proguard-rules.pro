# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keep,includedescriptorclasses class com.apalashkin.sudoku.**$$serializer { *; }
-keepclassmembers class com.apalashkin.sudoku.** {
    *** Companion;
}
-keepclasseswithmembers class com.apalashkin.sudoku.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *
-dontwarn androidx.room.paging.**
