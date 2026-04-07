package com.chaaru.bookbridge.screen

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun VintageTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    primaryColor: Color,
    secondaryColor: Color,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    minLines: Int = 1,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { 
            Text(
                label, 
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Serif, 
                    letterSpacing = 1.sp
                )
            ) 
        },
        modifier = modifier.fillMaxWidth(),
        visualTransformation = visualTransformation,
        trailingIcon = trailingIcon,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = primaryColor,
            unfocusedTextColor = primaryColor,
            focusedLabelColor = secondaryColor,
            unfocusedLabelColor = primaryColor.copy(alpha = 0.5f),
            cursorColor = secondaryColor,
            focusedBorderColor = secondaryColor,
            unfocusedBorderColor = primaryColor.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(4.dp),
        minLines = minLines,
        textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Serif)
    )
}
