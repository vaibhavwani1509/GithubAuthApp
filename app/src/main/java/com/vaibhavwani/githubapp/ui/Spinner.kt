package com.vaibhavwani.githubapp.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun <T> Spinner(
    modifier: Modifier = Modifier,
    dropDownModifier: Modifier = Modifier,
    items: List<T>,
    selectedItem: T?,
    onItemSelected: (T) -> Unit,
    selectedItemFactory: @Composable (Modifier, T) -> Unit,
    dropdownItemFactory: @Composable (T, Int) -> Unit,
) {
    var expanded: Boolean by remember { mutableStateOf(false) }

    Box(modifier = modifier.wrapContentSize(Alignment.TopStart)) {
        selectedItem?.let {
            selectedItemFactory(
                Modifier
                    .clickable { expanded = true },
                selectedItem
            )
        } ?: Row(
            modifier = modifier
                .padding(8.dp)
                .wrapContentSize()
        ) {
            Text(modifier = Modifier
                .clickable { expanded = true }, text = "Click here to select an item")
            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = dropDownModifier
        ) {
            items.forEachIndexed { index, element ->
                DropdownMenuItem(
                    text = { dropdownItemFactory(element, index) },
                    onClick = {
                        onItemSelected(items[index])
                        expanded = false
                    })
            }
        }
    }
}