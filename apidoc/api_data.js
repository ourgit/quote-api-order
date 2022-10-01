define({ "api": [
  {
    "type": "post",
    "url": "/v1/p/articles/fav/",
    "title": "06收藏",
    "name": "fav",
    "group": "Article",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "boolean",
            "optional": false,
            "field": "enable",
            "description": "<p>true收藏 false取消收藏</p>"
          },
          {
            "group": "Parameter",
            "type": "long",
            "optional": false,
            "field": "articleId",
            "description": "<p>文章ID</p>"
          }
        ]
      }
    },
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>200成功修改</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/ArticleController.java",
    "groupTitle": "Article"
  },
  {
    "type": "GET",
    "url": "/v1/p/articles/:articleId/",
    "title": "04文章详情",
    "name": "getArticle",
    "group": "Article",
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>200 请求成功</p>"
          },
          {
            "group": "Success 200",
            "type": "long",
            "optional": false,
            "field": "id",
            "description": "<p>文章id</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "title",
            "description": "<p>文章标题</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "author",
            "description": "<p>文章作者</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "digest",
            "description": "<p>文章摘要</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "headPic",
            "description": "<p>文图</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "content",
            "description": "<p>内容</p>"
          },
          {
            "group": "Success 200",
            "type": "long",
            "optional": false,
            "field": "categoryId",
            "description": "<p>分类ID</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "categoryName",
            "description": "<p>分类名字</p>"
          },
          {
            "group": "Success 200",
            "type": "long",
            "optional": false,
            "field": "views",
            "description": "<p>阅读数</p>"
          },
          {
            "group": "Success 200",
            "type": "long",
            "optional": false,
            "field": "favs",
            "description": "<p>收藏数</p>"
          },
          {
            "group": "Success 200",
            "type": "long",
            "optional": false,
            "field": "comments",
            "description": "<p>评论数</p>"
          },
          {
            "group": "Success 200",
            "type": "long",
            "optional": false,
            "field": "shares",
            "description": "<p>分享数</p>"
          },
          {
            "group": "Success 200",
            "type": "boolean",
            "optional": false,
            "field": "isFav",
            "description": "<p>是否已收藏 ，true收藏 ，false未收藏</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "createTime",
            "description": "<p>时间</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/ArticleController.java",
    "groupTitle": "Article"
  },
  {
    "type": "GET",
    "url": "/v1/p/param_config/?key=",
    "title": "07获取参数配置",
    "name": "getParamConfig",
    "group": "Article",
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>200 请求成功</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "value",
            "description": "<p>值</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "note",
            "description": "<p>中文备注</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/ArticleController.java",
    "groupTitle": "Article"
  },
  {
    "type": "GET",
    "url": "/v1/p/articles_by_category_id/?page=&categoryId=",
    "title": "02根据分类id获取文章列表",
    "name": "listArticles",
    "group": "Article",
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>200 请求成功</p>"
          },
          {
            "group": "Success 200",
            "type": "JsonObject",
            "optional": false,
            "field": "list",
            "description": "<p>列表</p>"
          },
          {
            "group": "Success 200",
            "type": "long",
            "optional": false,
            "field": "articleId",
            "description": "<p>文章id</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "title",
            "description": "<p>文章标题</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "author",
            "description": "<p>文章作者</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "source",
            "description": "<p>文章来源</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "publishTime",
            "description": "<p>文章发布时间</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "categoryName",
            "description": "<p>所属分类</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "publishStatusName",
            "description": "<p>发布状态</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/ArticleController.java",
    "groupTitle": "Article"
  },
  {
    "type": "GET",
    "url": "/v1/p/articles/?page=&size=",
    "title": "03根据分类名字获取文章列表",
    "name": "listArticles",
    "group": "Article",
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>200 请求成功</p>"
          },
          {
            "group": "Success 200",
            "type": "JsonObject",
            "optional": false,
            "field": "list",
            "description": "<p>列表</p>"
          },
          {
            "group": "Success 200",
            "type": "long",
            "optional": false,
            "field": "articleId",
            "description": "<p>文章id</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "title",
            "description": "<p>文章标题</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "author",
            "description": "<p>文章作者</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "source",
            "description": "<p>文章来源</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "publishTime",
            "description": "<p>文章发布时间</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "categoryName",
            "description": "<p>所属分类</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "publishStatusName",
            "description": "<p>发布状态</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/ArticleController.java",
    "groupTitle": "Article"
  },
  {
    "type": "GET",
    "url": "/v1/p/discovery_list/",
    "title": "01发现列表",
    "name": "listDiscovery",
    "group": "Article",
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>200 请求成功</p>"
          },
          {
            "group": "Success 200",
            "type": "JsonObject",
            "optional": false,
            "field": "list",
            "description": "<p>列表</p>"
          },
          {
            "group": "Success 200",
            "type": "long",
            "optional": false,
            "field": "articleId",
            "description": "<p>文章id</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "title",
            "description": "<p>文章标题</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "author",
            "description": "<p>文章作者</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "source",
            "description": "<p>文章来源</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "publishTime",
            "description": "<p>文章发布时间</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "categoryName",
            "description": "<p>所属分类</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "publishStatusName",
            "description": "<p>发布状态</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/ArticleController.java",
    "groupTitle": "Article"
  },
  {
    "type": "GET",
    "url": "/v1/p/recommend_articles/",
    "title": "05精选文章列表",
    "name": "listRecommendArticles",
    "group": "Article",
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>200 请求成功</p>"
          },
          {
            "group": "Success 200",
            "type": "JsonObject",
            "optional": false,
            "field": "list",
            "description": "<p>列表</p>"
          },
          {
            "group": "Success 200",
            "type": "long",
            "optional": false,
            "field": "articleId",
            "description": "<p>文章id</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "title",
            "description": "<p>文章标题</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "author",
            "description": "<p>文章作者</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "source",
            "description": "<p>文章来源</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "publishTime",
            "description": "<p>文章发布时间</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "categoryName",
            "description": "<p>所属分类</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "publishStatusName",
            "description": "<p>发布状态</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/ArticleController.java",
    "groupTitle": "Article"
  },
  {
    "type": "GET",
    "url": "/v1/n/carousels/?regionCode=&bizType=&clientType=",
    "title": "01查看轮播列表",
    "name": "listCarousel",
    "group": "Carousel",
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>200 请求成功</p>"
          },
          {
            "group": "Success 200",
            "type": "JsonArray",
            "optional": false,
            "field": "list",
            "description": "<p>列表</p>"
          },
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "id",
            "description": "<p>轮播id</p>"
          },
          {
            "group": "Success 200",
            "type": "string",
            "optional": false,
            "field": "name",
            "description": "<p>轮播名称</p>"
          },
          {
            "group": "Success 200",
            "type": "string",
            "optional": false,
            "field": "imgUrl",
            "description": "<p>图片链接地址</p>"
          },
          {
            "group": "Success 200",
            "type": "string",
            "optional": false,
            "field": "linkUrl",
            "description": "<p>链接地址</p>"
          },
          {
            "group": "Success 200",
            "type": "string",
            "optional": false,
            "field": "description",
            "description": "<p>备注</p>"
          },
          {
            "group": "Success 200",
            "type": "string",
            "optional": false,
            "field": "regionCode",
            "description": "<p>区域编号</p>"
          },
          {
            "group": "Success 200",
            "type": "string",
            "optional": false,
            "field": "regionName",
            "description": "<p>区域名字</p>"
          },
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "sort",
            "description": "<p>显示顺序</p>"
          }
        ],
        "Error 500": [
          {
            "group": "Error 500",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>500 未知错误</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/CarouselController.java",
    "groupTitle": "Carousel"
  },
  {
    "type": "GET",
    "url": "/v1/user/categories/?filter&cateType=",
    "title": "01分类列表",
    "name": "listCategories",
    "group": "Category",
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>200 请求成功</p>"
          },
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "pages",
            "description": "<p>页数</p>"
          },
          {
            "group": "Success 200",
            "type": "JsonArray",
            "optional": false,
            "field": "list",
            "description": "<p>列表</p>"
          },
          {
            "group": "Success 200",
            "type": "long",
            "optional": false,
            "field": "id",
            "description": "<p>分类id</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "name",
            "description": "<p>名称</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "imgUrl",
            "description": "<p>图片</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "poster",
            "description": "<p>海报图片</p>"
          },
          {
            "group": "Success 200",
            "type": "long",
            "optional": false,
            "field": "soldAmount",
            "description": "<p>已售数量</p>"
          },
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "show",
            "description": "<p>1显示2不显示</p>"
          },
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "sort",
            "description": "<p>排序顺序</p>"
          },
          {
            "group": "Success 200",
            "type": "JsonArray",
            "optional": false,
            "field": "children",
            "description": "<p>子分类列表</p>"
          },
          {
            "group": "Success 200",
            "type": "string",
            "optional": false,
            "field": "updateTime",
            "description": "<p>更新时间</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/CategoryController.java",
    "groupTitle": "Category"
  },
  {
    "type": "POST",
    "url": "/v1/p/wechat/pay_notify/",
    "title": "01支付回调接口",
    "name": "handleWechatPayNotify",
    "group": "Pay",
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>200</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/WechatPayV3Controller.java",
    "groupTitle": "Pay"
  },
  {
    "type": "GET",
    "url": "/v1/user/posts/:id/",
    "title": "03帖子详情",
    "name": "getPost",
    "group": "Post",
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>200 请求成功</p>"
          },
          {
            "group": "Success 200",
            "type": "Array",
            "optional": false,
            "field": "replyList",
            "description": "<p>跟贴内容</p>"
          },
          {
            "group": "Success 200",
            "type": "long",
            "optional": false,
            "field": "id",
            "description": "<p>id</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "title",
            "description": "<p>标题</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "userName",
            "description": "<p>作者</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "avatar",
            "description": "<p>头像</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "content",
            "description": "<p>内容</p>"
          },
          {
            "group": "Success 200",
            "type": "long",
            "optional": false,
            "field": "categoryId",
            "description": "<p>分类ID</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "categoryName",
            "description": "<p>分类名字</p>"
          },
          {
            "group": "Success 200",
            "type": "long",
            "optional": false,
            "field": "commentNumber",
            "description": "<p>跟贴数</p>"
          },
          {
            "group": "Success 200",
            "type": "long",
            "optional": false,
            "field": "replies",
            "description": "<p>回复数</p>"
          },
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "status",
            "description": "<p>1正常 -1隐藏</p>"
          },
          {
            "group": "Success 200",
            "type": "long",
            "optional": false,
            "field": "participants",
            "description": "<p>参与人数</p>"
          },
          {
            "group": "Success 200",
            "type": "long",
            "optional": false,
            "field": "likes",
            "description": "<p>点赞数</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "updateTime",
            "description": "<p>更新时间</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "createTime",
            "description": "<p>创建时间</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/PostController.java",
    "groupTitle": "Post"
  },
  {
    "type": "POST",
    "url": "/v1/user/post/like/",
    "title": "04点赞",
    "name": "likePost",
    "group": "Post",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "long",
            "optional": false,
            "field": "id",
            "description": "<p>id</p>"
          },
          {
            "group": "Parameter",
            "type": "boolean",
            "optional": false,
            "field": "like",
            "description": "<p>true为点赞 false取消点赞</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/PostController.java",
    "groupTitle": "Post"
  },
  {
    "type": "GET",
    "url": "/v1/user/post_categories/?filter&cateType=",
    "title": "01帖子分类列表",
    "name": "listPostCategories",
    "group": "Post",
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>200 请求成功</p>"
          },
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "pages",
            "description": "<p>页数</p>"
          },
          {
            "group": "Success 200",
            "type": "JsonArray",
            "optional": false,
            "field": "list",
            "description": "<p>列表</p>"
          },
          {
            "group": "Success 200",
            "type": "long",
            "optional": false,
            "field": "id",
            "description": "<p>分类id</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "name",
            "description": "<p>名称</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "imgUrl",
            "description": "<p>图片</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "poster",
            "description": "<p>海报图片</p>"
          },
          {
            "group": "Success 200",
            "type": "long",
            "optional": false,
            "field": "soldAmount",
            "description": "<p>已售数量</p>"
          },
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "show",
            "description": "<p>1显示2不显示</p>"
          },
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "sort",
            "description": "<p>排序顺序</p>"
          },
          {
            "group": "Success 200",
            "type": "JsonArray",
            "optional": false,
            "field": "children",
            "description": "<p>子分类列表</p>"
          },
          {
            "group": "Success 200",
            "type": "string",
            "optional": false,
            "field": "updateTime",
            "description": "<p>更新时间</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/PostController.java",
    "groupTitle": "Post"
  },
  {
    "type": "GET",
    "url": "/v1/user/post_list/?categoryId=",
    "title": "02贴子列表",
    "name": "listPosts",
    "group": "Post",
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>200 请求成功</p>"
          },
          {
            "group": "Success 200",
            "type": "JsonObject",
            "optional": false,
            "field": "list",
            "description": "<p>列表</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "title",
            "description": "<p>标题</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "userName",
            "description": "<p>作者</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "avatar",
            "description": "<p>头像</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "content",
            "description": "<p>内容</p>"
          },
          {
            "group": "Success 200",
            "type": "long",
            "optional": false,
            "field": "categoryId",
            "description": "<p>分类ID</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "categoryName",
            "description": "<p>分类名字</p>"
          },
          {
            "group": "Success 200",
            "type": "long",
            "optional": false,
            "field": "commentNumber",
            "description": "<p>跟贴数</p>"
          },
          {
            "group": "Success 200",
            "type": "long",
            "optional": false,
            "field": "replies",
            "description": "<p>回复数</p>"
          },
          {
            "group": "Success 200",
            "type": "long",
            "optional": false,
            "field": "participants",
            "description": "<p>参与人数</p>"
          },
          {
            "group": "Success 200",
            "type": "long",
            "optional": false,
            "field": "likes",
            "description": "<p>点赞数</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "updateTime",
            "description": "<p>更新时间</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "createTime",
            "description": "<p>创建时间</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/PostController.java",
    "groupTitle": "Post"
  },
  {
    "type": "GET",
    "url": "/v1/user/my_posts/",
    "title": "07我的帖子",
    "name": "myPosts",
    "group": "Post",
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>200 请求成功</p>"
          },
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "pages",
            "description": "<p>页数</p>"
          },
          {
            "group": "Success 200",
            "type": "JsonArray",
            "optional": false,
            "field": "list",
            "description": "<p>列表</p>"
          },
          {
            "group": "Success 200",
            "type": "long",
            "optional": false,
            "field": "id",
            "description": "<p>分类id</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "name",
            "description": "<p>名称</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "imgUrl",
            "description": "<p>图片</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "poster",
            "description": "<p>海报图片</p>"
          },
          {
            "group": "Success 200",
            "type": "long",
            "optional": false,
            "field": "soldAmount",
            "description": "<p>已售数量</p>"
          },
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "show",
            "description": "<p>1显示2不显示</p>"
          },
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "sort",
            "description": "<p>排序顺序</p>"
          },
          {
            "group": "Success 200",
            "type": "JsonArray",
            "optional": false,
            "field": "children",
            "description": "<p>子分类列表</p>"
          },
          {
            "group": "Success 200",
            "type": "string",
            "optional": false,
            "field": "updateTime",
            "description": "<p>更新时间</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/PostController.java",
    "groupTitle": "Post"
  },
  {
    "type": "POST",
    "url": "/v1/user/post/new/",
    "title": "03发帖",
    "name": "newPost",
    "group": "Post",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "title",
            "description": "<p>标题</p>"
          },
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "content",
            "description": "<p>内容</p>"
          },
          {
            "group": "Parameter",
            "type": "long",
            "optional": false,
            "field": "categoryId",
            "description": "<p>分类ID</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/PostController.java",
    "groupTitle": "Post"
  },
  {
    "type": "POST",
    "url": "/v1/user/post/reply/",
    "title": "05回复",
    "name": "replyPost",
    "group": "Post",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "long",
            "optional": false,
            "field": "id",
            "description": "<p>id</p>"
          },
          {
            "group": "Parameter",
            "type": "long",
            "optional": true,
            "field": "quoteId",
            "description": "<p>引用某个回复</p>"
          },
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "content",
            "description": "<p>content</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/PostController.java",
    "groupTitle": "Post"
  },
  {
    "type": "POST",
    "url": "/v1/user/post/",
    "title": "06删除贴子/回复",
    "name": "replyPost",
    "group": "Post",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "long",
            "optional": false,
            "field": "id",
            "description": "<p>id</p>"
          },
          {
            "group": "Parameter",
            "type": "int",
            "optional": false,
            "field": "postType",
            "description": "<p>1主贴 2跟贴/回复</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/PostController.java",
    "groupTitle": "Post"
  },
  {
    "type": "POST",
    "url": "/v1/user/change_phone_number/",
    "title": "15修改手机号码",
    "name": "changePhoneNumber",
    "group": "User",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "String",
            "optional": true,
            "field": "note",
            "description": "<p>备注，需要分两步，第一步，为输入密码【可选】，以及新手机号与新手机号短信验证码，第二步，请求原手机号的短信验证码，调用这个接口， /v1/user/request_user_vcode/ 03请求给用户号码发短信，最终将所有参数一起送过来</p>"
          },
          {
            "group": "Parameter",
            "type": "string",
            "optional": true,
            "field": "password",
            "description": "<p>密码，如果有设置的话</p>"
          },
          {
            "group": "Parameter",
            "type": "string",
            "optional": false,
            "field": "newPhoneNumber",
            "description": "<p>新手机号</p>"
          },
          {
            "group": "Parameter",
            "type": "string",
            "optional": false,
            "field": "oldPhoneNumberVcode",
            "description": "<p>原手机号短信验证码</p>"
          },
          {
            "group": "Parameter",
            "type": "string",
            "optional": false,
            "field": "newPhoneNumberVcode",
            "description": "<p>新手机号短信验证码</p>"
          }
        ]
      }
    },
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>200</p>"
          }
        ],
        "Error 40001": [
          {
            "group": "Error 40001",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>40001无效的参数</p>"
          }
        ],
        "Error 40002": [
          {
            "group": "Error 40002",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>40002 无效的短信验证码</p>"
          }
        ],
        "Error 40003": [
          {
            "group": "Error 40003",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>40003 密码错误</p>"
          }
        ],
        "Error 40004": [
          {
            "group": "Error 40004",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>40004无效的手机号码</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/MemberController.java",
    "groupTitle": "User"
  },
  {
    "type": "POST",
    "url": "/v1/user/clear_all_msg/",
    "title": "17清除全部消息列表",
    "name": "clearMsg",
    "group": "User",
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>200</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/MemberController.java",
    "groupTitle": "User"
  },
  {
    "type": "POST",
    "url": "/v1/user/clear_selected_msg/",
    "title": "18清除选中消息列表",
    "name": "clearSelectedMsg",
    "group": "User",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "Array",
            "optional": false,
            "field": "list",
            "description": ""
          }
        ]
      }
    },
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>200</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/MemberController.java",
    "groupTitle": "User"
  },
  {
    "type": "GET",
    "url": "/v1/user/member_balance/?balanceType=",
    "title": "11用户余额信息",
    "name": "getMemberBalance",
    "group": "User",
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>请求返回码</p>"
          },
          {
            "group": "Success 200",
            "type": "JsonArray",
            "optional": false,
            "field": "list",
            "description": "<p>所持有币种的余额列表</p>"
          },
          {
            "group": "Success 200",
            "type": "BigDecimal",
            "optional": false,
            "field": "leftBalance",
            "description": "<p>可用余额</p>"
          },
          {
            "group": "Success 200",
            "type": "BigDecimal",
            "optional": false,
            "field": "freezeBalance",
            "description": "<p>冻结余额</p>"
          },
          {
            "group": "Success 200",
            "type": "BigDecimal",
            "optional": false,
            "field": "totalBalance",
            "description": "<p>总额</p>"
          },
          {
            "group": "Success 200",
            "type": "BigDecimal",
            "optional": false,
            "field": "expireTime",
            "description": "<p>失效时间</p>"
          }
        ],
        "Error 403": [
          {
            "group": "Error 403",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>403 没有权限使用该功能</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/MemberController.java",
    "groupTitle": "User"
  },
  {
    "type": "get",
    "url": "/v1/user/?code=",
    "title": "05获取用户信息",
    "name": "getUser",
    "group": "User",
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "string",
            "optional": false,
            "field": "phoneNumber",
            "description": "<p>手机号</p>"
          },
          {
            "group": "Success 200",
            "type": "long",
            "optional": false,
            "field": "score",
            "description": "<p>积分</p>"
          },
          {
            "group": "Success 200",
            "type": "long",
            "optional": false,
            "field": "leftMoney",
            "description": "<p>余额</p>"
          },
          {
            "group": "Success 200",
            "type": "string",
            "optional": false,
            "field": "authStatus",
            "description": "<p>认证状态  -1已拒绝 0未申请  1进行中 2已通过</p>"
          },
          {
            "group": "Success 200",
            "type": "string",
            "optional": false,
            "field": "levelName",
            "description": "<p>等级</p>"
          },
          {
            "group": "Success 200",
            "type": "string",
            "optional": false,
            "field": "realName",
            "description": "<p>实名</p>"
          },
          {
            "group": "Success 200",
            "type": "string",
            "optional": false,
            "field": "nickName",
            "description": "<p>昵称</p>"
          },
          {
            "group": "Success 200",
            "type": "string",
            "optional": false,
            "field": "uid",
            "description": "<p>用户Id</p>"
          },
          {
            "group": "Success 200",
            "type": "string",
            "optional": false,
            "field": "birthday",
            "description": "<p>生日</p>"
          },
          {
            "group": "Success 200",
            "type": "string",
            "optional": false,
            "field": "idCardNo",
            "description": "<p>身份证号码</p>"
          },
          {
            "group": "Success 200",
            "type": "string",
            "optional": false,
            "field": "licenseNo",
            "description": "<p>营业执照</p>"
          },
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "gender",
            "description": "<p>0：未知、1：男、2：女</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "city",
            "description": "<p>城市</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "province",
            "description": "<p>省份</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "country",
            "description": "<p>国家</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "shopName",
            "description": "<p>品牌</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "contactPhoneNumber",
            "description": "<p>联系电话</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "contactAddress",
            "description": "<p>联系地址</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "businessItems",
            "description": "<p>经营类目</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "images",
            "description": "<p>图片，多张，以逗号隔开</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "dealerCode",
            "description": "<p>业务员/代理商编号(如果自己是代理商或业务员才会有这个字段)</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "dealerName",
            "description": "<p>绑定的业务员名字</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "dealerPhoneNumber",
            "description": "<p>绑定的业务员联系电话</p>"
          },
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "couponCount",
            "description": "<p>可用优惠券数量</p>"
          },
          {
            "group": "Success 200",
            "type": "Object",
            "optional": false,
            "field": "profile",
            "description": "<p>用户认证相关对象</p>"
          },
          {
            "group": "Success 200",
            "type": "long",
            "optional": false,
            "field": "applyTime",
            "description": "<p>申请时间</p>"
          },
          {
            "group": "Success 200",
            "type": "long",
            "optional": false,
            "field": "approveTime",
            "description": "<p>认证通过时间</p>"
          }
        ],
        "Error 40001": [
          {
            "group": "Error 40001",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>40001 未找到对应的用户</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/MemberController.java",
    "groupTitle": "User"
  },
  {
    "type": "GET",
    "url": "/v1/user/is_login/",
    "title": "13是否已登录",
    "name": "isLogin",
    "group": "User",
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>200</p>"
          },
          {
            "group": "Success 200",
            "type": "boolean",
            "optional": false,
            "field": "login",
            "description": "<p>true已登录 false未登录</p>"
          }
        ],
        "Error 40001": [
          {
            "group": "Error 40001",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>40001 参数错误</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/MemberController.java",
    "groupTitle": "User"
  },
  {
    "type": "GET",
    "url": "/v1/user/is_valid_account/:accountName/",
    "title": "08检验帐号是否有效",
    "examples": [
      {
        "title": "说明:1个ip一天限制10个该请求",
        "content": "http://192.168.2.100/v1/is_valid_account/:accountName/",
        "type": "json"
      }
    ],
    "name": "isValidAccount",
    "group": "User",
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>200</p>"
          }
        ],
        "Error 40001": [
          {
            "group": "Error 40001",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>40001该手机号未注册</p>"
          }
        ],
        "Error 40002": [
          {
            "group": "Error 40002",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>40002超过当日最大调用次数</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/MemberController.java",
    "groupTitle": "User"
  },
  {
    "type": "GET",
    "url": "/v1/user/is_phonenumber_used/:phoneNumber/",
    "title": "06检测手机号码是否已被注册",
    "name": "is_phonenumber_used",
    "group": "User",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "string",
            "optional": false,
            "field": "phoneNumber",
            "description": "<p>手机号</p>"
          }
        ]
      }
    },
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>200 未注册</p>"
          },
          {
            "group": "Success 200",
            "type": "boolean",
            "optional": false,
            "field": "used",
            "description": "<p>true已注册，false未注册</p>"
          }
        ],
        "Error 40001": [
          {
            "group": "Error 40001",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>40001 无效的手机号码</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/MemberController.java",
    "groupTitle": "User"
  },
  {
    "type": "GET",
    "url": "/v1/user/balance_logs/?bizType=&itemId=&page=",
    "title": "14余额/积分明细列表",
    "name": "listBalanceLogs",
    "group": "User",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "optional": false,
            "field": "itemId",
            "description": "<p>1现金 2积分</p>"
          },
          {
            "group": "Parameter",
            "optional": false,
            "field": "bizType",
            "description": "<p>99积分奖励 100团购返现</p>"
          }
        ]
      }
    },
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>200成功修改</p>"
          },
          {
            "group": "Success 200",
            "type": "JsonArray",
            "optional": false,
            "field": "list",
            "description": "<p>列表</p>"
          },
          {
            "group": "Success 200",
            "type": "double",
            "optional": false,
            "field": "leftBalance",
            "description": "<p>可用余额</p>"
          },
          {
            "group": "Success 200",
            "type": "double",
            "optional": false,
            "field": "freezeBalance",
            "description": "<p>冻结余额</p>"
          },
          {
            "group": "Success 200",
            "type": "double",
            "optional": false,
            "field": "totalBalance",
            "description": "<p>总额</p>"
          },
          {
            "group": "Success 200",
            "type": "double",
            "optional": false,
            "field": "changeAmount",
            "description": "<p>当次变动金额</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "note",
            "description": "<p>备注</p>"
          },
          {
            "group": "Success 200",
            "type": "long",
            "optional": false,
            "field": "createTime",
            "description": "<p>时间</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/MemberController.java",
    "groupTitle": "User"
  },
  {
    "type": "GET",
    "url": "/v1/user/msg_list/?page=&msgType=",
    "title": "16我的消息列表",
    "name": "listMsg",
    "group": "User",
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>200</p>"
          },
          {
            "group": "Success 200",
            "type": "JsonArray",
            "optional": false,
            "field": "list",
            "description": "<p>列表</p>"
          },
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "status",
            "description": "<p>1未读 2已读</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "title",
            "description": "<p>标题</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "content",
            "description": "<p>内容</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "linkUrl",
            "description": "<p>链接地址</p>"
          },
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "msgType",
            "description": "<p>消息类型 1余额变动 2交易提醒 3系统通知</p>"
          },
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "itemId",
            "description": "<p>余额类型，跟余额中的一样</p>"
          },
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "changeAmount",
            "description": "<p>余额变动数量</p>"
          },
          {
            "group": "Success 200",
            "type": "long",
            "optional": false,
            "field": "createTime",
            "description": "<p>时间</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/MemberController.java",
    "groupTitle": "User"
  },
  {
    "type": "GET",
    "url": "/v1/user/my_login_logs/?page=",
    "title": "12我的登录日志",
    "name": "listMyLoginLog",
    "group": "User",
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>200 请求成功</p>"
          },
          {
            "group": "Success 200",
            "type": "JsonArray",
            "optional": false,
            "field": "list",
            "description": "<p>列表</p>"
          },
          {
            "group": "Success 200",
            "type": "long",
            "optional": false,
            "field": "id",
            "description": ""
          },
          {
            "group": "Success 200",
            "type": "long",
            "optional": false,
            "field": "memberId",
            "description": ""
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "loginIp",
            "description": "<p>登录ip</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "loginPlace",
            "description": "<p>登录地点</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "createdTime",
            "description": "<p>登录时间</p>"
          }
        ],
        "Success 403": [
          {
            "group": "Success 403",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>没有权限使用该功能</p>"
          }
        ],
        "Error 505": [
          {
            "group": "Error 505",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>未知错误</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/MemberController.java",
    "groupTitle": "User"
  },
  {
    "type": "POST",
    "url": "/v1/user/login/",
    "title": "02登录",
    "name": "login",
    "group": "User",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "string",
            "optional": false,
            "field": "username",
            "description": "<p>用户名</p>"
          },
          {
            "group": "Parameter",
            "type": "string",
            "optional": false,
            "field": "password",
            "description": "<p>密码</p>"
          },
          {
            "group": "Parameter",
            "type": "string",
            "optional": true,
            "field": "vCode",
            "description": "<p>短信验证码，短信登录的时候使用</p>"
          }
        ]
      }
    },
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "long",
            "optional": false,
            "field": "id",
            "description": "<p>用户id</p>"
          },
          {
            "group": "Success 200",
            "type": "string",
            "optional": false,
            "field": "accountName",
            "description": "<p>帐号名</p>"
          },
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "status",
            "description": "<p>用户状态 1正常2锁定</p>"
          },
          {
            "group": "Success 200",
            "type": "string",
            "optional": false,
            "field": "realName",
            "description": "<p>真实姓名</p>"
          },
          {
            "group": "Success 200",
            "type": "string",
            "optional": false,
            "field": "phoneNumber",
            "description": "<p>手机号</p>"
          }
        ],
        "Error 40001": [
          {
            "group": "Error 40001",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>40001 传进来的参数有误</p>"
          }
        ],
        "Error 40003": [
          {
            "group": "Error 40003",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>40003 用户名或密码错误</p>"
          }
        ],
        "Error 40005": [
          {
            "group": "Error 40005",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>40005 用户被锁定</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/MemberController.java",
    "groupTitle": "User"
  },
  {
    "type": "POST",
    "url": "/v1/user/loginPassword/",
    "title": "04修改登录密码",
    "name": "loginPassword",
    "group": "User",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "string",
            "optional": false,
            "field": "oldPassword",
            "description": "<p>旧密码</p>"
          },
          {
            "group": "Parameter",
            "type": "string",
            "optional": false,
            "field": "newPassword",
            "description": "<p>新密码</p>"
          }
        ]
      }
    },
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>200 成功修改</p>"
          }
        ],
        "Error 40001": [
          {
            "group": "Error 40001",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>40001 密码不合法6-20位</p>"
          }
        ],
        "Error 40002": [
          {
            "group": "Error 40002",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>40002 旧密码验证不通过</p>"
          }
        ],
        "Error 40004": [
          {
            "group": "Error 40004",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>40004 用户不存在</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/MemberController.java",
    "groupTitle": "User"
  },
  {
    "type": "POST",
    "url": "/v1/user/logout/",
    "title": "03注销",
    "name": "logout",
    "group": "User",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "long",
            "optional": false,
            "field": "id",
            "description": "<p>用户id</p>"
          }
        ]
      }
    },
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>200 注销成功.</p>"
          }
        ],
        "Error 40001": [
          {
            "group": "Error 40001",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>40001 参数错误,未提供token</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/MemberController.java",
    "groupTitle": "User"
  },
  {
    "type": "POST",
    "url": "/v1/user/reset_login_password/",
    "title": "09重置登录密码",
    "name": "resetLoginPassword",
    "group": "User",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "string",
            "optional": false,
            "field": "accountName",
            "description": "<p>帐号</p>"
          },
          {
            "group": "Parameter",
            "type": "string",
            "optional": false,
            "field": "vcode",
            "description": "<p>短信验证码</p>"
          },
          {
            "group": "Parameter",
            "type": "string",
            "optional": false,
            "field": "newPassword",
            "description": "<p>新密码</p>"
          }
        ]
      }
    },
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>200</p>"
          }
        ],
        "Error 40001": [
          {
            "group": "Error 40001",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>40001无效的参数</p>"
          }
        ],
        "Error 40002": [
          {
            "group": "Error 40002",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>40002 无效的短信验证码</p>"
          }
        ],
        "Error 40003": [
          {
            "group": "Error 40003",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>40003 无效的密码</p>"
          }
        ],
        "Error 40004": [
          {
            "group": "Error 40004",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>40004无效的手机号码</p>"
          }
        ],
        "Error 40005": [
          {
            "group": "Error 40005",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>40005 该帐号不存在</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/MemberController.java",
    "groupTitle": "User"
  },
  {
    "type": "POST",
    "url": "/v1/user/set_login_password/",
    "title": "10设置/修改登录密码",
    "name": "setLoginPassword",
    "group": "User",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "string",
            "optional": true,
            "field": "oldPassword",
            "description": "<p>旧密码</p>"
          },
          {
            "group": "Parameter",
            "type": "string",
            "optional": false,
            "field": "password",
            "description": "<p>新密码</p>"
          },
          {
            "group": "Parameter",
            "type": "string",
            "optional": true,
            "field": "vcode",
            "description": "<p>短信验证码</p>"
          },
          {
            "group": "Parameter",
            "type": "long",
            "optional": false,
            "field": "uid",
            "description": "<p>用戶Id</p>"
          }
        ]
      }
    },
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>200</p>"
          }
        ],
        "Error 40001": [
          {
            "group": "Error 40001",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>40001 无效的参数</p>"
          }
        ],
        "Error 40004": [
          {
            "group": "Error 40004",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>40004 该帐号不存在</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/MemberController.java",
    "groupTitle": "User"
  },
  {
    "type": "post",
    "url": "/v1/user/new/",
    "title": "01用户注册",
    "name": "signUp",
    "group": "User",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "string",
            "optional": false,
            "field": "phoneNumber",
            "description": "<p>手机号</p>"
          },
          {
            "group": "Parameter",
            "type": "string",
            "optional": true,
            "field": "vCode",
            "description": "<p>短信验证码，预留</p>"
          },
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "loginPassword",
            "description": "<p>登录密码 6-20位，不允许包含非法字符</p>"
          },
          {
            "group": "Parameter",
            "type": "String",
            "optional": true,
            "field": "dealerCode",
            "description": "<p>业务员编号</p>"
          },
          {
            "group": "Parameter",
            "type": "String",
            "optional": true,
            "field": "name",
            "description": "<p>昵称或名字，最长10个字符</p>"
          }
        ]
      }
    },
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>200成功创建</p>"
          }
        ],
        "Error 40003": [
          {
            "group": "Error 40003",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>40001 参数错误</p>"
          }
        ],
        "Error 40001": [
          {
            "group": "Error 40001",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>40002 帐号已被注册</p>"
          }
        ],
        "Error 40002": [
          {
            "group": "Error 40002",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>40003 无效的短信验证码</p>"
          }
        ],
        "Error 40004": [
          {
            "group": "Error 40004",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>40004 登录密码无效</p>"
          }
        ],
        "Error 40006": [
          {
            "group": "Error 40006",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>40006 无效的手机号码</p>"
          }
        ],
        "Error 40007": [
          {
            "group": "Error 40007",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>40007 达到注册的限制次数</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/MemberController.java",
    "groupTitle": "User"
  },
  {
    "type": "post",
    "url": "/v1/user/",
    "title": "07修改用户资料",
    "name": "updateUser",
    "group": "User",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "string",
            "optional": true,
            "field": "realName",
            "description": "<p>姓名 不超过6位</p>"
          },
          {
            "group": "Parameter",
            "type": "string",
            "optional": true,
            "field": "nickName",
            "description": "<p>昵称 不超过10位</p>"
          },
          {
            "group": "Parameter",
            "type": "string",
            "optional": true,
            "field": "birthday",
            "description": "<p>生日</p>"
          },
          {
            "group": "Parameter",
            "type": "string",
            "optional": true,
            "field": "avatar",
            "description": "<p>头像</p>"
          },
          {
            "group": "Parameter",
            "type": "int",
            "optional": true,
            "field": "gender",
            "description": "<p>性别 0：未知、1：男、2：女</p>"
          },
          {
            "group": "Parameter",
            "type": "string",
            "optional": true,
            "field": "city",
            "description": "<p>城市</p>"
          },
          {
            "group": "Parameter",
            "type": "string",
            "optional": true,
            "field": "province",
            "description": "<p>省份</p>"
          },
          {
            "group": "Parameter",
            "type": "string",
            "optional": true,
            "field": "country",
            "description": "<p>国家</p>"
          }
        ]
      }
    },
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>200成功修改</p>"
          }
        ],
        "Error 40001": [
          {
            "group": "Error 40001",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>40001 参数错误</p>"
          }
        ],
        "Error 40002": [
          {
            "group": "Error 40002",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>40002 密码无效，密码为6到20位</p>"
          }
        ],
        "Error 403": [
          {
            "group": "Error 403",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>403 没有权限，请重新登录</p>"
          }
        ],
        "Error 40004": [
          {
            "group": "Error 40004",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>40004 用户不存在</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/MemberController.java",
    "groupTitle": "User"
  }
] });
