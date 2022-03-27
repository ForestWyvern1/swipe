package com.example.swipetest

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.swipetest.databinding.ActivityMainBinding
import com.example.swipetest.databinding.ViewImageCardBinding
import com.example.swipetest.databinding.ViewPagerBinding


class MainActivity : AppCompatActivity() {

    // Список "картинок"
    private val images: MutableList<Int> = mutableListOf(
        R.color.purple_200,
        R.color.purple_500,
        R.color.purple_700,
        R.color.teal_200,
        R.color.teal_700
    )

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadImages()
    }

    // метод загрузки стека картинок
    private fun loadImages() {
        // в начале чистим фрейм от всех (старых) вьюшек (чтобы можно было повторно вызывать этот метод)
        binding.imagesFrame.removeAllViews()

        // проходимся по списку
        for (i in images.reversed()) {
            // создаем биндинг для файла с пейджерем (его мы будем пихать во фрейм)
            val viewPager = ViewPagerBinding.bind(
                LayoutInflater
                    .from(this)
                    .inflate(R.layout.view_pager, binding.imagesFrame, false)
            )

            viewPager.root.apply {
                // дефолт
                orientation = ViewPager2.ORIENTATION_HORIZONTAL
                adapter = ViewPagerAdapter(
                    this@MainActivity,
                    listOf(R.color.transparent, i, R.color.transparent),
                    images.indexOf(i) // нужно определить индекс нынешнего элемента чтобы добавить отступ в адаптере
                )

                // нужен именно этот метод чтобы при повторном вызове этого метода не было анимки переключения на 1ый элемент
                // второй параметр отрубает анимку
                setCurrentItem(1, false)

                // слушаем смену страницы
                registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                    override fun onPageScrollStateChanged(state: Int) {
                        super.onPageScrollStateChanged(state)

                        // SCROLL_STATE_IDLE - состояние покоя, в него переходит пейджер когда мы поменяли страницу
                        // отслеживаем, что он в покое и сейчас не 1ая страница
                        // значит он свайпнул
                        // значит удаляем этот элемент из массива и вызываем метод заново чтобы обновить фрейм с картинками
                        if (state == ViewPager2.SCROLL_STATE_IDLE && viewPager.root.currentItem != 1) {
                            images.remove(i)
                            if (images.isEmpty())
                                Toast.makeText(this@MainActivity, "End", Toast.LENGTH_SHORT).show()

                            loadImages()
                        }
                    }

                    override fun onPageSelected(position: Int) {
                        super.onPageSelected(position)

                        // юзаем этот метод, а не прошлый чтобы отслеживать на какой элемент (position) перешли
                        // соответственно, отслеживать в какую сторону был свайп
                        if (position != 1)
                            Toast.makeText(
                                this@MainActivity,
                                if (position == 2) "to left" else "to right",
                                Toast.LENGTH_SHORT
                            ).show()
                    }
                })

                // изменение наклона страниц в зависимости от того, насколько далеко её оттянули от центра
                // 45 - максимальный угол наклона
                // можно просто запомнить
                setPageTransformer { page, position ->
                    page.rotation = 45 * position
                }
            }

            // добавляем корень нашего биндинга (ViewPager) во фрейм
            binding.imagesFrame.addView(viewPager.root)
        }
    }
}

class ViewPagerAdapter(
    private val context: Context,
    private val images: List<Int>,
    private val imagePosition: Int, // нужно передавать его сюда для добавления отступов
) : RecyclerView.Adapter<ViewPagerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        LayoutInflater
            .from(context)
            .inflate(R.layout.view_image_card, parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val binding = ViewImageCardBinding.bind(holder.itemView)

        binding.imageView.apply {
            setBackgroundColor(
                ContextCompat.getColor(context, images[position])
            )

            // изменение верхнего отступа в зависимости от позиции
            layoutParams = (layoutParams as ViewGroup.MarginLayoutParams).apply {
                topMargin = when (imagePosition) {
                    0 -> 128
                    1 -> 64
                    else -> 0
                }
            }
        }
    }

    override fun getItemCount() = images.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}