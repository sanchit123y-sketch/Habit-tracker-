package com.example.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.theme.SubjectEnglishColor
import com.example.ui.theme.SubjectHindiColor
import com.example.ui.theme.SubjectMathsColor
import com.example.ui.theme.SubjectOtherColor
import com.example.ui.theme.SubjectScienceColor
import com.example.ui.theme.SubjectSstColor

enum class StudySubject(
    val displayName: String,
    val accentColor: Color,
    val icon: ImageVector
) {
    MATHS("Maths", SubjectMathsColor, Icons.Rounded.Calculate),
    ENGLISH("English", SubjectEnglishColor, Icons.Rounded.MenuBook),
    SCIENCE("Science", SubjectScienceColor, Icons.Rounded.Science),
    HINDI("Hindi", SubjectHindiColor, Icons.Rounded.Translate),
    SST("SST", SubjectSstColor, Icons.Rounded.Public),
    OTHER("Other", SubjectOtherColor, Icons.Rounded.Bookmark);

    companion object {
        fun fromName(name: String): StudySubject {
            return entries.firstOrNull {
                it.name.equals(name, ignoreCase = true) ||
                    it.displayName.equals(name, ignoreCase = true)
            } ?: OTHER
        }
    }
}
