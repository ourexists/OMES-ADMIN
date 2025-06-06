/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

/**
 * 用于打包聚合版，该文件不会存在于构建后的目录 
 */
 
layui.define(function(exports){
  var cache = layui.cache;
  layui.config({
    dir: cache.dir.replace(/lay\/dest\/$/, '')
  });
  exports('layui.all', layui.v);
});