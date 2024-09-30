//package com.demo.jsontoview.UI
//
//import BackgroundConfig
//import DimensionConfig
//import DrawableConfig
//import LayoutType
//import OrientationConfig
//import PaddingConfig
//import Props
//import PropsDrawable
//import android.content.Context
//import com.demo.jsontoview.CustomViewGroup2
//import com.demo.jsontoview.FView
//
//class FViewComment(private val context: Context,private val customViewGroup: CustomViewGroup2) {
//
//    fun createCommentView(text: String):FView {
//        val horizontalLayout = horizontalViewGroup()
//        horizontalLayout.children.add(imageFView())
//        horizontalLayout.children.add(contentView(text))
//        horizontalLayout.setCustomViewGroup(context = context, customViewGroup = customViewGroup)
//        return horizontalLayout
//    }
//
//    private fun verticalViewGroup(): FView {
//        val props = Props(
//            id = "buttonComment",
//            background = BackgroundConfig(
//                type = 1,
//                color = "#f2f4f7"
//            ),
//            width = DimensionConfig(-1, UnitConfig.Px),
//            height = DimensionConfig(-2, UnitConfig.Px),
//            gap = 5,
//            radius = 30,
//            padding = PaddingConfig(30, 30, 30, 30),
//            orientation = OrientationConfig.Vertical,
//            layoutType = LayoutType.Continues,
//        )
//
//        val viewGroup = FView(
//            ViewTypeConfig.ViewGroup,
//            props,
//            children = mutableListOf()
//        )
//        return viewGroup
//    }
//
//    private fun horizontalViewGroup(): FView {
//        val props = Props(
//            id = "buttonComment",
//            test = "taora",
//            width = DimensionConfig(-1, UnitConfig.Px),
//            height = DimensionConfig(-2, UnitConfig.Px),
//            gap = 20,
//            radius = 8,
//            orientation = OrientationConfig.Horizontal,
//            layoutType = LayoutType.Continues,
//        )
//
//        return FView(
//            ViewTypeConfig.ViewGroup,
//            props,
//            children = mutableListOf(
//            )
//        )
//    }
//
//
//    private fun imageFView(): FView {
//        val props = Props(
//            width = DimensionConfig(100, UnitConfig.Px),
//            height = DimensionConfig(100, UnitConfig.Px),
//            drawable = DrawableConfig(
//                type = TypeConfig.Image,
//                data = "https://d1hjkbq40fs2x4.cloudfront.net/2016-01-31/files/1045-2.jpg",
//                props = PropsDrawable(
//                    radius = 200,
//                    scaleType = ScaleType.Center
//                )
//            ),
//            orientation = OrientationConfig.Horizontal,
//            layoutType = LayoutType.Continues,
//        )
//        return FView(
//            ViewTypeConfig.ViewGroup,
//            props
//        )
//    }
//
//    private fun contentView(content: String): FView {
//
//        val props = Props(
//            width = DimensionConfig(-1, UnitConfig.Px),
//            height = DimensionConfig(-2, UnitConfig.Px),
//            drawable = DrawableConfig(
//                type = TypeConfig.Text,
//                data = "Mạnh Tú",
//                props = PropsDrawable(
//                    textSize = 40,
//                    textType = TextType.Bold,
//                    textColor = "#000000"
//                )
//            ),
//            orientation = OrientationConfig.Horizontal,
//            layoutType = LayoutType.Continues,
//        )
//        val title = FView(
//            ViewTypeConfig.ViewGroup,
//            props
//        )
//
//        val propsContent = Props(
//            width = DimensionConfig(-1, UnitConfig.Px),
//            height = DimensionConfig(-2, UnitConfig.Px),
//            drawable = DrawableConfig(
//                type = TypeConfig.Text,
//                data = content,
//                props = PropsDrawable(
//                    textSize = 40,
//                    textColor = "#000000"
//                )
//            ),
//            orientation = OrientationConfig.Horizontal,
//            layoutType = LayoutType.Continues,
//        )
//
//        val content = FView(
//            ViewTypeConfig.ViewGroup,
//            propsContent
//        )
//
//        val verticalLayout = verticalViewGroup()
//        verticalLayout.children.add(title)
//        verticalLayout.children.add(content)
//        return verticalLayout
//
//    }
//
//
//}