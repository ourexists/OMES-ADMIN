/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

layui.define([], function (exports) {
    let $ = layui.$
    let addStyle = false
    //基础样式
    let setStyle = function (optionsDefault) {
        if(addStyle)return true
        let sheet = (function() {
            let styleObj = document.createElement("style");
            styleObj.appendChild(document.createTextNode(""));
            document.head.appendChild(styleObj);
            return styleObj.sheet;
        })();
        sheet.insertRule(optionsDefault.el+`{
            display: flex;
            flex-direction: column;
            align-items: center;
        }`, 0);
        sheet.insertRule(`.`+optionsDefault.cssPre+`node_box{
            display: flex;
            flex-direction: row;
            justify-content: space-between;
        }`, 0);
        sheet.insertRule(`.`+optionsDefault.cssPre+`node {
            display: flex;
            flex-direction: column;
            align-items: center;
            position: relative;
        }`, 0);
        sheet.insertRule(`.`+optionsDefault.cssPre+`node_content {
            /*min-height:`+optionsDefault.lineMinHeight+`px;*/
            display: flex;
            flex-direction: row;
            justify-content: center;
            align-items: center;
        }`, 0);

        sheet.insertRule(`.`+optionsDefault.cssPre+`node_line {
            width: 1px;
            min-height: `+optionsDefault.lineMinHeight+`px;
            background-color: grey;
        }`, 0);
        sheet.insertRule(`.`+optionsDefault.cssPre+`node_children {
            display: flex;
            flex-direction: column;
            align-items: center;
        }`, 0);
        sheet.insertRule(`.`+optionsDefault.cssPre+`node_children_box{
            display: flex;
            flex-direction: row;
            justify-content: center;
            align-items: flex-start;
        }`, 0);
        sheet.insertRule(`.`+optionsDefault.cssPre+`node_children_hr {
            width: 100%;
            display: flex;
            flex-direction: row;
        }`, 0);
        sheet.insertRule(`.`+optionsDefault.cssPre+`node_hr {
            width: 50%;
            background-color: grey;
            height: 1px;
        }`, 0);
    }

    //设置节点内容
    let content = function(node){
        return `<button type="button" class="layui-btn layui-btn-xs">`+node.title+`</button>`
    }
    //刷新
    let reload =function(nodes=optionsDefault.data){
        let html = buildNodes(nodes)
        $(optionsDefault.el).html(html)

        $(optionsDefault.el+' .'+optionsDefault.cssPre+'node_line').first().remove()
        $(optionsDefault.el+' .'+optionsDefault.cssPre+'node_line').last().remove()

        $(optionsDefault.el+' .'+optionsDefault.cssPre+'node_children_box > .'+optionsDefault.cssPre+'node_box').not(':first-child').css('margin-left','50px')

        layui.each($(optionsDefault.el+' .'+optionsDefault.cssPre+'node_children_box > .'+optionsDefault.cssPre+'node_box'),function(){
            let height = $(this).parent().height()
            $(this).css('height',height+'px')

            layui.each($(this).children('.'+optionsDefault.cssPre+'node').children(),function(){
                height -= $(this).height()
            })

            height = $(this).children('.'+optionsDefault.cssPre+'node').children('.'+optionsDefault.cssPre+'node_line').last().height()+height
            $(this).children('.'+optionsDefault.cssPre+'node').children('.'+optionsDefault.cssPre+'node_line').last().css('height',height+'px')
        })


        layui.each($(optionsDefault.el+' .'+optionsDefault.cssPre+'node_children_box'),function(){
            let width = $(this).width()

            layui.each($(this).children('.'+optionsDefault.cssPre+'node_box').children(),function(){
                let marginLeft = $(this).css('margin-left')
                marginLeft = marginLeft.replace('px','')-0
                width += marginLeft
            })
            $(this).css('width',width+'px')


            $(this).prev('.'+optionsDefault.cssPre+'node_children_hr').css('width',width+'px')
            $(this).next('.'+optionsDefault.cssPre+'node_children_hr').css('width',width+'px')
            width -= $(this).children('.'+optionsDefault.cssPre+'node_box').first().width()/2
            width -= $(this).children('.'+optionsDefault.cssPre+'node_box').last().width()/2

            $(this).prev('.'+optionsDefault.cssPre+'node_children_hr').children('.'+optionsDefault.cssPre+'node_hr').css('width',width+'px')
            $(this).next('.'+optionsDefault.cssPre+'node_children_hr').children('.'+optionsDefault.cssPre+'node_hr').css('width',width+'px')
        })

        layui.each($(optionsDefault.el+' .'+optionsDefault.cssPre+'node_children_box'),function(){
            let width = $(this).width()
            let width1 = $(this).children('.'+optionsDefault.cssPre+'node_box').first().width()/2
            width -= width1
            width -= $(this).children('.'+optionsDefault.cssPre+'node_box').last().width()/2

            $(this).prev('.'+optionsDefault.cssPre+'node_children_hr').children('.'+optionsDefault.cssPre+'node_hr').css('width',width+'px')
            $(this).prev('.'+optionsDefault.cssPre+'node_children_hr').children('.'+optionsDefault.cssPre+'node_hr').css('margin-left',width1+'px')
            $(this).next('.'+optionsDefault.cssPre+'node_children_hr').children('.'+optionsDefault.cssPre+'node_hr').css('width',width+'px')
            $(this).next('.'+optionsDefault.cssPre+'node_children_hr').children('.'+optionsDefault.cssPre+'node_hr').css('margin-left',width1+'px')
        })

        $(optionsDefault.el).parent().css('min-width',$(optionsDefault.el).width()+'px')
        $(optionsDefault.el).parent().css('min-height',$(optionsDefault.el).height()+'px')
    }
    //渲染流程
    let buildNodes = function(nodes) {
        let html = ''

        layui.each(nodes, function (index, item) {
            html += `<div class="`+optionsDefault.cssPre+`node_box">
                                <div class="`+optionsDefault.cssPre+`node">
                                    <div class="`+optionsDefault.cssPre+`node_line"></div>`

            html += `<div class="`+optionsDefault.cssPre+`node_content">`
            html += optionsDefault.nodeContent(item)//获取节点内容
            html += `</div>`

            html += `<div class="`+optionsDefault.cssPre+`node_line"></div>`

            html += getChildrenNode(item)//获取孩子节点

            html += `</div></div>`
        })


        return html
    }
    //子级节点
    let getChildrenNode = function(node){
        node.children = ('undefined'===typeof node.children)?[]:node.children
        node.convergent = ('undefined'===typeof node.convergent)?null:node.convergent

        let html = `<div class="`+optionsDefault.cssPre+`node_children">`

        //多分支时开始的横线
        if(node.children.length>1){
            html += `<div class="`+optionsDefault.cssPre+`node_children_hr"><div class="`+optionsDefault.cssPre+`node_hr"></div></div>`
        }

        html +=  `<div class="`+optionsDefault.cssPre+`node_children_box">`
        html += buildNodes(node.children)
        html += `</div>`

        //多分支结束后的横线与竖线
        if(node.children.length>1){
            html +=  `<div class="`+optionsDefault.cssPre+`node_children_hr"><div class="`+optionsDefault.cssPre+`node_hr"></div></div>`
            html += `<div class="`+optionsDefault.cssPre+`node_line"></div>`
        }

        //收敛节点
        if(node.convergent){
            html += `<div class="`+optionsDefault.cssPre+`convergent">`
            html += buildNodes([node.convergent])
            html += `</div>`
        }


        html += `</div>`
        return html
    }

    let optionsDefault = {
        cssPre:'',
        lineMinHeight:50,//竖连接线最小长度
        el: '',//父元素容器
        data:[],//流程数据
        nodeContent:content,//孩子节点渲染方法
        reload:reload
    }


    let func = {
        render:function(options=optionsDefault){
            optionsDefault = {...optionsDefault,...options}
            setStyle(optionsDefault)//设置基础样式
            optionsDefault.reload = reload

            optionsDefault.reload(optionsDefault.data)
            return optionsDefault
        }
    }

    exports('flowNode', func);

})
