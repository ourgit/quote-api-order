define({ "api": [
  {
    "type": "GET",
    "url": "/v1/o/group_order/:id/",
    "title": "05团购订单详情",
    "name": "getGroupOrder",
    "group": "Group-Order",
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
            "description": "<p>商品id</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "name",
            "description": "<p>名称</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/GroupOrderController.java",
    "groupTitle": "Group-Order"
  },
  {
    "type": "GET",
    "url": "/v1/o/group_order_stat/:productId/",
    "title": "07统计数据",
    "name": "groupOrderStat",
    "group": "Group-Order",
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "productName",
            "description": ""
          },
          {
            "group": "Success 200",
            "type": "long",
            "optional": false,
            "field": "orders",
            "description": "<p>订单数量</p>"
          },
          {
            "group": "Success 200",
            "type": "long",
            "optional": false,
            "field": "totalMoney",
            "description": "<p>订单总金额</p>"
          },
          {
            "group": "Success 200",
            "type": "long",
            "optional": false,
            "field": "buyers",
            "description": "<p>购买人数</p>"
          },
          {
            "group": "Success 200",
            "type": "long",
            "optional": false,
            "field": "directInviteOrders",
            "description": "<p>老客订单数</p>"
          },
          {
            "group": "Success 200",
            "type": "long",
            "optional": false,
            "field": "indirectInviteOrders",
            "description": "<p>新客订单数</p>"
          },
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
    "filename": "app/controllers/GroupOrderController.java",
    "groupTitle": "Group-Order"
  },
  {
    "type": "GET",
    "url": "/v1/o/group_orders/?page=",
    "title": "01订单列表",
    "name": "listGroupOrders",
    "group": "Group-Order",
    "success": {
      "fields": {
        "Success 200": [
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
            "description": "<p>订单列表</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/GroupOrderController.java",
    "groupTitle": "Group-Order"
  },
  {
    "type": "GET",
    "url": "/v1/o/my_customer_orders/?page=&filter=",
    "title": "06我的客户团购列表",
    "name": "listMyCustomerOrders",
    "group": "Group-Order",
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "Array",
            "optional": false,
            "field": "list",
            "description": "<p>列表</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "userName",
            "description": "<p>用户名字</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "dealerName",
            "description": "<p>推荐人</p>"
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
            "field": "source",
            "description": "<p>来源</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "phoneNumber",
            "description": "<p>手机号码</p>"
          },
          {
            "group": "Success 200",
            "type": "long",
            "optional": false,
            "field": "createTime",
            "description": "<p>下单时间</p>"
          },
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "orderType",
            "description": "<p>1为单购  2为团购</p>"
          },
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "grouponId",
            "description": "<p>0为开团  1为参团</p>"
          },
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "grouponStatus",
            "description": "<p>拼团状态</p>"
          },
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
    "filename": "app/controllers/GroupOrderController.java",
    "groupTitle": "Group-Order"
  },
  {
    "type": "GET",
    "url": "/v1/o/my_group_orders/?page=",
    "title": "02我的团购订单列表",
    "name": "listMyGroupOrders",
    "group": "Group-Order",
    "success": {
      "fields": {
        "Success 200": [
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
            "description": "<p>订单列表</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/GroupOrderController.java",
    "groupTitle": "Group-Order"
  },
  {
    "type": "POST",
    "url": "/v1/o/pay_group_order/",
    "title": "04支付订单",
    "name": "payGroupOrder",
    "group": "Group-Order",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "long",
            "optional": false,
            "field": "orderId",
            "description": "<p>订单ID</p>"
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
    "filename": "app/controllers/GroupOrderController.java",
    "groupTitle": "Group-Order"
  },
  {
    "type": "POST",
    "url": "/v1/o/groupon_order/new/",
    "title": "03团购下单",
    "name": "placeGrouponOrder",
    "group": "Group-Order",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "long",
            "optional": true,
            "field": "productId",
            "description": "<p>商品ID 团购发起人需要送productId这个参数</p>"
          },
          {
            "group": "Parameter",
            "type": "long",
            "optional": true,
            "field": "grouponId",
            "description": "<p>团购ID 参与团购需要送grouponId这个参数</p>"
          },
          {
            "group": "Parameter",
            "type": "long",
            "optional": true,
            "field": "dealerId",
            "description": "<p>dealerId</p>"
          },
          {
            "group": "Parameter",
            "type": "int",
            "optional": false,
            "field": "orderType",
            "description": "<p>1单购 2团购</p>"
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
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "transactionId",
            "description": "<p>流水号</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/GroupOrderController.java",
    "groupTitle": "Group-Order"
  },
  {
    "type": "POST",
    "url": "/v1/o/part_return_order_detail/",
    "title": "02申请/取消部分退货",
    "name": "applyReturnOrderDetail",
    "group": "OrderReturn",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "long",
            "optional": false,
            "field": "orderId",
            "description": "<p>订单ID</p>"
          },
          {
            "group": "Parameter",
            "type": "long",
            "optional": true,
            "field": "returnOrderId",
            "description": "<p>售后ID</p>"
          },
          {
            "group": "Parameter",
            "type": "int",
            "optional": false,
            "field": "status",
            "description": "<p>10申请售后 -1取消售后</p>"
          },
          {
            "group": "Parameter",
            "type": "Array",
            "optional": false,
            "field": "list",
            "description": "<p>list</p>"
          },
          {
            "group": "Parameter",
            "type": "long",
            "optional": false,
            "field": "orderDetailId",
            "description": "<p>orderDetailId</p>"
          },
          {
            "group": "Parameter",
            "type": "long",
            "optional": false,
            "field": "returnNumber",
            "description": "<p>退货数量</p>"
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
            "description": "<p>40001 参数错误</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/OrderReturnController.java",
    "groupTitle": "OrderReturn"
  },
  {
    "type": "POST",
    "url": "/v1/o/part_return_order_detail/",
    "title": "02申请/取消部分退货",
    "name": "applyReturnOrderDetail",
    "group": "OrderReturn",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "long",
            "optional": false,
            "field": "orderId",
            "description": "<p>订单ID</p>"
          },
          {
            "group": "Parameter",
            "type": "long",
            "optional": true,
            "field": "returnOrderId",
            "description": "<p>售后ID</p>"
          },
          {
            "group": "Parameter",
            "type": "int",
            "optional": false,
            "field": "status",
            "description": "<p>10申请售后 -1取消售后</p>"
          },
          {
            "group": "Parameter",
            "type": "Array",
            "optional": false,
            "field": "list",
            "description": "<p>list</p>"
          },
          {
            "group": "Parameter",
            "type": "long",
            "optional": false,
            "field": "orderDetailId",
            "description": "<p>orderDetailId</p>"
          },
          {
            "group": "Parameter",
            "type": "long",
            "optional": false,
            "field": "returnNumber",
            "description": "<p>退货数量</p>"
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
            "description": "<p>40001 参数错误</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/store/StoreOrderReturnController.java",
    "groupTitle": "OrderReturn"
  },
  {
    "type": "POST",
    "url": "/v1/o/calc_part_return_money/",
    "title": "03计算退货金额",
    "name": "calcReturnMoney",
    "group": "OrderReturn",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "long",
            "optional": false,
            "field": "orderId",
            "description": "<p>订单ID</p>"
          },
          {
            "group": "Parameter",
            "type": "Array",
            "optional": false,
            "field": "list",
            "description": "<p>list</p>"
          },
          {
            "group": "Parameter",
            "type": "long",
            "optional": false,
            "field": "orderDetailId",
            "description": "<p>orderDetailId</p>"
          },
          {
            "group": "Parameter",
            "type": "long",
            "optional": false,
            "field": "returnNumber",
            "description": "<p>退货数量</p>"
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
            "description": "<p>40001 参数错误</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/OrderReturnController.java",
    "groupTitle": "OrderReturn"
  },
  {
    "type": "POST",
    "url": "/v1/o/calc_part_return_money/",
    "title": "03计算退货金额",
    "name": "calcReturnMoney",
    "group": "OrderReturn",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "long",
            "optional": false,
            "field": "orderId",
            "description": "<p>订单ID</p>"
          },
          {
            "group": "Parameter",
            "type": "Array",
            "optional": false,
            "field": "list",
            "description": "<p>list</p>"
          },
          {
            "group": "Parameter",
            "type": "long",
            "optional": false,
            "field": "orderDetailId",
            "description": "<p>orderDetailId</p>"
          },
          {
            "group": "Parameter",
            "type": "long",
            "optional": false,
            "field": "returnNumber",
            "description": "<p>退货数量</p>"
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
            "description": "<p>40001 参数错误</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/store/StoreOrderReturnController.java",
    "groupTitle": "OrderReturn"
  },
  {
    "type": "POST",
    "url": "/v1/o/cancel_order/",
    "title": "01取消订单",
    "name": "cancelOrder",
    "group": "OrderReturn",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "long",
            "optional": false,
            "field": "orderId",
            "description": "<p>订单Id</p>"
          },
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "operation",
            "description": "<p>为cancel取消</p>"
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
            "description": "<p>40001 参数错误</p>"
          }
        ],
        "Error 40002": [
          {
            "group": "Error 40002",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>40002 交易密码错误</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/OrderReturnController.java",
    "groupTitle": "OrderReturn"
  },
  {
    "type": "POST",
    "url": "/v1/o/cancel_order/",
    "title": "01取消订单",
    "name": "cancelOrder",
    "group": "OrderReturn",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "long",
            "optional": false,
            "field": "orderId",
            "description": "<p>订单Id</p>"
          },
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "operation",
            "description": "<p>为cancel取消</p>"
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
            "description": "<p>40001 参数错误</p>"
          }
        ],
        "Error 40002": [
          {
            "group": "Error 40002",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>40002 交易密码错误</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/store/StoreOrderReturnController.java",
    "groupTitle": "OrderReturn"
  },
  {
    "type": "POST",
    "url": "/v1/o/return_order_logis/",
    "title": "06填写退货物流",
    "name": "fillOutLogisForReturnOrder",
    "group": "OrderReturn",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "returnsNo",
            "description": "<p>退货ID</p>"
          },
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "logisNo",
            "description": "<p>物流单号</p>"
          },
          {
            "group": "Parameter",
            "type": "String",
            "optional": true,
            "field": "logisName",
            "description": "<p>物流公司</p>"
          },
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "note",
            "description": "<p>备注</p>"
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
            "description": "<p>40001 参数错误</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/OrderReturnController.java",
    "groupTitle": "OrderReturn"
  },
  {
    "type": "POST",
    "url": "/v1/o/return_order_logis/",
    "title": "06填写退货物流",
    "name": "fillOutLogisForReturnOrder",
    "group": "OrderReturn",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "returnsNo",
            "description": "<p>退货ID</p>"
          },
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "logisNo",
            "description": "<p>物流单号</p>"
          },
          {
            "group": "Parameter",
            "type": "String",
            "optional": true,
            "field": "logisName",
            "description": "<p>物流公司</p>"
          },
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "note",
            "description": "<p>备注</p>"
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
            "description": "<p>40001 参数错误</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/store/StoreOrderReturnController.java",
    "groupTitle": "OrderReturn"
  },
  {
    "type": "GET",
    "url": "/v1/o/return_orders_list/:returnOrderId/",
    "title": "10退货单详情",
    "name": "getReturnOrderDetail",
    "group": "OrderReturn",
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
            "type": "String",
            "optional": false,
            "field": "returnsNo",
            "description": "<p>退货单号</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "expressNo",
            "description": "<p>快递单号</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "logisName",
            "description": "<p>物流公司</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "logisticsLastDesc",
            "description": "<p>物流最后状态描述</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "orderNo",
            "description": "<p>订单编号</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "totalMoney",
            "description": "<p>总金额</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "realPay",
            "description": "<p>实付</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "logisticsFee",
            "description": "<p>运费</p>"
          },
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "productCount",
            "description": "<p>商品数量</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "statusName",
            "description": "<p>状态名字</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "description",
            "description": "<p>描述</p>"
          },
          {
            "group": "Success 200",
            "type": "JsonArray",
            "optional": false,
            "field": "detailList",
            "description": "<p>商品列表</p>"
          },
          {
            "group": "Success 200",
            "type": "string",
            "optional": false,
            "field": "productName",
            "description": "<p>商品名字</p>"
          },
          {
            "group": "Success 200",
            "type": "string",
            "optional": false,
            "field": "productImgUrl",
            "description": "<p>商品图片</p>"
          },
          {
            "group": "Success 200",
            "type": "string",
            "optional": false,
            "field": "skuName",
            "description": "<p>商品属性描述</p>"
          },
          {
            "group": "Success 200",
            "type": "long",
            "optional": false,
            "field": "returnSubmitTime",
            "description": "<p>申请时间</p>"
          },
          {
            "group": "Success 200",
            "type": "long",
            "optional": false,
            "field": "handlingReturnEndTime",
            "description": "<p>售后申请最长处理时间</p>"
          },
          {
            "group": "Success 200",
            "type": "long",
            "optional": false,
            "field": "returnSubmitEndTime",
            "description": "<p>退货最长寄回时间</p>"
          },
          {
            "group": "Success 200",
            "type": "long",
            "optional": false,
            "field": "handlingRefundTime",
            "description": "<p>退款时间</p>"
          },
          {
            "group": "Success 200",
            "type": "long",
            "optional": false,
            "field": "handlingRefundEndTime",
            "description": "<p>退款最长处理时间</p>"
          },
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "status",
            "description": "<p>-3拒绝退款  -2 申请被驳回  -1取消退货  10退货退款待审核  20待退回  30退款待审核  40已退款  200处理完毕</p>"
          },
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "state",
            "description": "<p>1仅退款 2退货退款</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/OrderReturnController.java",
    "groupTitle": "OrderReturn"
  },
  {
    "type": "GET",
    "url": "/v1/o/return_orders_list/:returnOrderId/",
    "title": "10退货单详情",
    "name": "getReturnOrderDetail",
    "group": "OrderReturn",
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
            "type": "String",
            "optional": false,
            "field": "returnsNo",
            "description": "<p>退货单号</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "expressNo",
            "description": "<p>快递单号</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "logisName",
            "description": "<p>物流公司</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "logisticsLastDesc",
            "description": "<p>物流最后状态描述</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "orderNo",
            "description": "<p>订单编号</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "totalMoney",
            "description": "<p>总金额</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "realPay",
            "description": "<p>实付</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "logisticsFee",
            "description": "<p>运费</p>"
          },
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "productCount",
            "description": "<p>商品数量</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "statusName",
            "description": "<p>状态名字</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "description",
            "description": "<p>描述</p>"
          },
          {
            "group": "Success 200",
            "type": "JsonArray",
            "optional": false,
            "field": "detailList",
            "description": "<p>商品列表</p>"
          },
          {
            "group": "Success 200",
            "type": "string",
            "optional": false,
            "field": "productName",
            "description": "<p>商品名字</p>"
          },
          {
            "group": "Success 200",
            "type": "string",
            "optional": false,
            "field": "productImgUrl",
            "description": "<p>商品图片</p>"
          },
          {
            "group": "Success 200",
            "type": "string",
            "optional": false,
            "field": "skuName",
            "description": "<p>商品属性描述</p>"
          },
          {
            "group": "Success 200",
            "type": "long",
            "optional": false,
            "field": "returnSubmitTime",
            "description": "<p>申请时间</p>"
          },
          {
            "group": "Success 200",
            "type": "long",
            "optional": false,
            "field": "handlingReturnEndTime",
            "description": "<p>售后申请最长处理时间</p>"
          },
          {
            "group": "Success 200",
            "type": "long",
            "optional": false,
            "field": "returnSubmitEndTime",
            "description": "<p>退货最长寄回时间</p>"
          },
          {
            "group": "Success 200",
            "type": "long",
            "optional": false,
            "field": "handlingRefundTime",
            "description": "<p>退款时间</p>"
          },
          {
            "group": "Success 200",
            "type": "long",
            "optional": false,
            "field": "handlingRefundEndTime",
            "description": "<p>退款最长处理时间</p>"
          },
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "status",
            "description": "<p>-3拒绝退款  -2 申请被驳回  -1取消退货  10退货退款待审核  20待退回  30退款待审核  40已退款  200处理完毕</p>"
          },
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "state",
            "description": "<p>1仅退款 2退货退款</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/store/StoreOrderReturnController.java",
    "groupTitle": "OrderReturn"
  },
  {
    "type": "POST",
    "url": "/v1/o/handle_order_refund/",
    "title": "07处理退款",
    "name": "handleOrderRefund",
    "group": "OrderReturn",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "long",
            "optional": false,
            "field": "returnOrderId",
            "description": "<p>退货id</p>"
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
            "description": "<p>200 请求成功</p>"
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
            "description": "<p>40002 该退货不存在</p>"
          }
        ],
        "Error 40004": [
          {
            "group": "Error 40004",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>40004 该退货申请不是处于可同意退货的状态</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/OrderReturnController.java",
    "groupTitle": "OrderReturn"
  },
  {
    "type": "POST",
    "url": "/v1/o/handle_order_refund/",
    "title": "07处理退款",
    "name": "handleOrderRefund",
    "group": "OrderReturn",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "long",
            "optional": false,
            "field": "returnOrderId",
            "description": "<p>退货id</p>"
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
            "description": "<p>200 请求成功</p>"
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
            "description": "<p>40002 该退货不存在</p>"
          }
        ],
        "Error 40004": [
          {
            "group": "Error 40004",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>40004 该退货申请不是处于可同意退货的状态</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/store/StoreOrderReturnController.java",
    "groupTitle": "OrderReturn"
  },
  {
    "type": "POST",
    "url": "/v1/o/handle_order_return/",
    "title": "05处理退货",
    "name": "handleOrderReturn",
    "group": "OrderReturn",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "long",
            "optional": false,
            "field": "returnOrderId",
            "description": "<p>退货id</p>"
          },
          {
            "group": "Parameter",
            "type": "int",
            "optional": false,
            "field": "returnOrderStatus",
            "description": "<p>20同意退货 -2拒绝退货</p>"
          },
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "auditReason",
            "description": "<p>拒绝原因</p>"
          },
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "auditRemark",
            "description": "<p>拒绝备注</p>"
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
            "description": "<p>200 请求成功</p>"
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
            "description": "<p>40002 该退货不存在</p>"
          }
        ],
        "Error 40004": [
          {
            "group": "Error 40004",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>40004 该退货申请不是处于可同意退货的状态</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/OrderReturnController.java",
    "groupTitle": "OrderReturn"
  },
  {
    "type": "POST",
    "url": "/v1/o/handle_order_return/",
    "title": "05处理退货",
    "name": "handleOrderReturn",
    "group": "OrderReturn",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "long",
            "optional": false,
            "field": "returnOrderId",
            "description": "<p>退货id</p>"
          },
          {
            "group": "Parameter",
            "type": "int",
            "optional": false,
            "field": "returnOrderStatus",
            "description": "<p>20同意退货 -2拒绝退货</p>"
          },
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "auditReason",
            "description": "<p>拒绝原因</p>"
          },
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "auditRemark",
            "description": "<p>拒绝备注</p>"
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
            "description": "<p>200 请求成功</p>"
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
            "description": "<p>40002 该退货不存在</p>"
          }
        ],
        "Error 40004": [
          {
            "group": "Error 40004",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>40004 该退货申请不是处于可同意退货的状态</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/store/StoreOrderReturnController.java",
    "groupTitle": "OrderReturn"
  },
  {
    "type": "GET",
    "url": "/v1/o/return_orders_lists/",
    "title": "08我的退货列表",
    "name": "listReturnOrders",
    "group": "OrderReturn",
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
            "description": "<p>订单列表</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "returnsNo",
            "description": "<p>退货单号</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "expressNo",
            "description": "<p>快递单号</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "logisName",
            "description": "<p>物流公司</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "logisticsLastDesc",
            "description": "<p>物流最后状态描述</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "orderNo",
            "description": "<p>订单编号</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "totalMoney",
            "description": "<p>总金额</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "realPay",
            "description": "<p>实付</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "logisticsFee",
            "description": "<p>运费</p>"
          },
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "productCount",
            "description": "<p>商品数量</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "statusName",
            "description": "<p>状态名字</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "description",
            "description": "<p>描述</p>"
          },
          {
            "group": "Success 200",
            "type": "JsonArray",
            "optional": false,
            "field": "detailList",
            "description": "<p>商品列表</p>"
          },
          {
            "group": "Success 200",
            "type": "string",
            "optional": false,
            "field": "productName",
            "description": "<p>商品名字</p>"
          },
          {
            "group": "Success 200",
            "type": "string",
            "optional": false,
            "field": "productImgUrl",
            "description": "<p>商品图片</p>"
          },
          {
            "group": "Success 200",
            "type": "string",
            "optional": false,
            "field": "skuName",
            "description": "<p>商品属性描述</p>"
          },
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "status",
            "description": "<p>0未付款,1已付款,2已发货,3已签收,-1退货申请,-2退货中,-3已退货,-4取消交易 -5撤销申请</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/OrderReturnController.java",
    "groupTitle": "OrderReturn"
  },
  {
    "type": "GET",
    "url": "/v1/o/return_orders_lists/",
    "title": "08我的退货列表",
    "name": "listReturnOrders",
    "group": "OrderReturn",
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
            "description": "<p>订单列表</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "returnsNo",
            "description": "<p>退货单号</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "expressNo",
            "description": "<p>快递单号</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "logisName",
            "description": "<p>物流公司</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "logisticsLastDesc",
            "description": "<p>物流最后状态描述</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "orderNo",
            "description": "<p>订单编号</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "totalMoney",
            "description": "<p>总金额</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "realPay",
            "description": "<p>实付</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "logisticsFee",
            "description": "<p>运费</p>"
          },
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "productCount",
            "description": "<p>商品数量</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "statusName",
            "description": "<p>状态名字</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "description",
            "description": "<p>描述</p>"
          },
          {
            "group": "Success 200",
            "type": "JsonArray",
            "optional": false,
            "field": "detailList",
            "description": "<p>商品列表</p>"
          },
          {
            "group": "Success 200",
            "type": "string",
            "optional": false,
            "field": "productName",
            "description": "<p>商品名字</p>"
          },
          {
            "group": "Success 200",
            "type": "string",
            "optional": false,
            "field": "productImgUrl",
            "description": "<p>商品图片</p>"
          },
          {
            "group": "Success 200",
            "type": "string",
            "optional": false,
            "field": "skuName",
            "description": "<p>商品属性描述</p>"
          },
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "status",
            "description": "<p>0未付款,1已付款,2已发货,3已签收,-1退货申请,-2退货中,-3已退货,-4取消交易 -5撤销申请</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/store/StoreOrderReturnController.java",
    "groupTitle": "OrderReturn"
  },
  {
    "type": "GET",
    "url": "/v1/o/shop_return_orders/",
    "title": "09店铺退货列表",
    "name": "listShopReturnOrders",
    "group": "OrderReturn",
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
            "description": "<p>订单列表</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "returnsNo",
            "description": "<p>退货单号</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "expressNo",
            "description": "<p>快递单号</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "logisName",
            "description": "<p>物流公司</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "logisticsLastDesc",
            "description": "<p>物流最后状态描述</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "orderNo",
            "description": "<p>订单编号</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "totalMoney",
            "description": "<p>总金额</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "realPay",
            "description": "<p>实付</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "logisticsFee",
            "description": "<p>运费</p>"
          },
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "productCount",
            "description": "<p>商品数量</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "statusName",
            "description": "<p>状态名字</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "description",
            "description": "<p>描述</p>"
          },
          {
            "group": "Success 200",
            "type": "JsonArray",
            "optional": false,
            "field": "detailList",
            "description": "<p>商品列表</p>"
          },
          {
            "group": "Success 200",
            "type": "string",
            "optional": false,
            "field": "productName",
            "description": "<p>商品名字</p>"
          },
          {
            "group": "Success 200",
            "type": "string",
            "optional": false,
            "field": "productImgUrl",
            "description": "<p>商品图片</p>"
          },
          {
            "group": "Success 200",
            "type": "string",
            "optional": false,
            "field": "skuName",
            "description": "<p>商品属性描述</p>"
          },
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "status",
            "description": "<p>0未付款,1已付款,2已发货,3已签收,-1退货申请,-2退货中,-3已退货,-4取消交易 -5撤销申请</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/OrderReturnController.java",
    "groupTitle": "OrderReturn"
  },
  {
    "type": "GET",
    "url": "/v1/o/shop_return_orders/",
    "title": "09店铺退货列表",
    "name": "listShopReturnOrders",
    "group": "OrderReturn",
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
            "description": "<p>订单列表</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "returnsNo",
            "description": "<p>退货单号</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "expressNo",
            "description": "<p>快递单号</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "logisName",
            "description": "<p>物流公司</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "logisticsLastDesc",
            "description": "<p>物流最后状态描述</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "orderNo",
            "description": "<p>订单编号</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "totalMoney",
            "description": "<p>总金额</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "realPay",
            "description": "<p>实付</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "logisticsFee",
            "description": "<p>运费</p>"
          },
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "productCount",
            "description": "<p>商品数量</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "statusName",
            "description": "<p>状态名字</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "description",
            "description": "<p>描述</p>"
          },
          {
            "group": "Success 200",
            "type": "JsonArray",
            "optional": false,
            "field": "detailList",
            "description": "<p>商品列表</p>"
          },
          {
            "group": "Success 200",
            "type": "string",
            "optional": false,
            "field": "productName",
            "description": "<p>商品名字</p>"
          },
          {
            "group": "Success 200",
            "type": "string",
            "optional": false,
            "field": "productImgUrl",
            "description": "<p>商品图片</p>"
          },
          {
            "group": "Success 200",
            "type": "string",
            "optional": false,
            "field": "skuName",
            "description": "<p>商品属性描述</p>"
          },
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "status",
            "description": "<p>0未付款,1已付款,2已发货,3已签收,-1退货申请,-2退货中,-3已退货,-4取消交易 -5撤销申请</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/store/StoreOrderReturnController.java",
    "groupTitle": "OrderReturn"
  },
  {
    "type": "POST",
    "url": "/v1/o/reject_order_refund/",
    "title": "08驳回退款请求",
    "name": "rejectOrderRefund",
    "group": "OrderReturn",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "long",
            "optional": false,
            "field": "returnOrderId",
            "description": "<p>退货id</p>"
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
            "description": "<p>200 请求成功</p>"
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
            "description": "<p>40002 该退货不存在</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/OrderReturnController.java",
    "groupTitle": "OrderReturn"
  },
  {
    "type": "POST",
    "url": "/v1/o/reject_order_refund/",
    "title": "08驳回退款请求",
    "name": "rejectOrderRefund",
    "group": "OrderReturn",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "long",
            "optional": false,
            "field": "returnOrderId",
            "description": "<p>退货id</p>"
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
            "description": "<p>200 请求成功</p>"
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
            "description": "<p>40002 该退货不存在</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/store/StoreOrderReturnController.java",
    "groupTitle": "OrderReturn"
  },
  {
    "type": "GET",
    "url": "/v1/o/orders_by_no/:orderNo/",
    "title": "02订单详情",
    "name": "getOrderDetailByOrderNo",
    "group": "Order_H5",
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
            "type": "String",
            "optional": false,
            "field": "orderNo",
            "description": "<p>订单编号</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "totalMoney",
            "description": "<p>总金额</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "realPay",
            "description": "<p>实付</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "logisticsFee",
            "description": "<p>运费</p>"
          },
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "productCount",
            "description": "<p>商品数量</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "statusName",
            "description": "<p>状态名字</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "description",
            "description": "<p>描述</p>"
          },
          {
            "group": "Success 200",
            "type": "JsonArray",
            "optional": false,
            "field": "detailList",
            "description": "<p>商品列表</p>"
          },
          {
            "group": "Success 200",
            "type": "string",
            "optional": false,
            "field": "productName",
            "description": "<p>商品名字</p>"
          },
          {
            "group": "Success 200",
            "type": "string",
            "optional": false,
            "field": "productImgUrl",
            "description": "<p>商品图片</p>"
          },
          {
            "group": "Success 200",
            "type": "string",
            "optional": false,
            "field": "skuName",
            "description": "<p>商品属性描述</p>"
          },
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "state",
            "description": "<p>售后状态 0 未发起售后 1 申请售后 -1 售后已取消 2 处理中 200 处理完毕</p>"
          },
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "status",
            "description": "<p>0未付款,1已付款,2已发货,3已签收,-1退货申请,-2退货中,-3已退货,-4取消交易 -5撤销申请</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "isMix",
            "description": "<p>是否混搭</p>"
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
            "description": "<p>40002 找不到该订单</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/OrderNoAuthController.java",
    "groupTitle": "Order_H5"
  },
  {
    "type": "POST",
    "url": "/v1/o/pay_order_no/",
    "title": "01支付代客订单",
    "name": "payOrder",
    "group": "Order_H5",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "long",
            "optional": false,
            "field": "orderId",
            "description": "<p>订单ID</p>"
          },
          {
            "group": "Parameter",
            "type": "int",
            "optional": false,
            "field": "payMethod",
            "description": "<p>1银行卡支付2支付宝支付3微信支付 6微信支付H5 7支付宝H5支付8小程序支付</p>"
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
          },
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "note",
            "description": "<p>如果是支付宝直接跳到支付宝支付的网页，如果是微信支付返回一个待支付的二维码，字段名codeUrl</p>"
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
            "description": "<p>40002 该订单不存在</p>"
          }
        ],
        "Error 40003": [
          {
            "group": "Error 40003",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>40003 该订单不是待支付状态,不能支付</p>"
          }
        ],
        "Error 40004": [
          {
            "group": "Error 40004",
            "type": "int",
            "optional": false,
            "field": "errCodeDesc",
            "description": "<p>微信支付错误提示</p>"
          }
        ],
        "Error 40005": [
          {
            "group": "Error 40005",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>40005 请输入短信验证码</p>"
          }
        ],
        "Error 40006": [
          {
            "group": "Error 40006",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>40006 余额不足</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/OrderNoAuthController.java",
    "groupTitle": "Order_H5"
  },
  {
    "type": "POST",
    "url": "/v1/o/calc_order/",
    "title": "17计算下单金额与优惠",
    "name": "calcOrder",
    "group": "Order",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "string",
            "optional": true,
            "field": "description",
            "description": "<p>备注</p>"
          },
          {
            "group": "Parameter",
            "type": "JsonArray",
            "optional": false,
            "field": "list",
            "description": "<p>购买的商品列表</p>"
          },
          {
            "group": "Parameter",
            "type": "long",
            "optional": false,
            "field": "productId",
            "description": "<p>商品ID</p>"
          },
          {
            "group": "Parameter",
            "type": "long",
            "optional": false,
            "field": "skuId",
            "description": "<p>商品skuID,为0时表示只有一个标准的产品，没有sku</p>"
          },
          {
            "group": "Parameter",
            "type": "long",
            "optional": false,
            "field": "amount",
            "description": "<p>购买数量</p>"
          },
          {
            "group": "Parameter",
            "type": "String",
            "optional": true,
            "field": "remark",
            "description": "<p>备注，每个单品的备注，预留</p>"
          },
          {
            "group": "Parameter",
            "type": "long",
            "optional": true,
            "field": "memberCouponId",
            "description": "<p>用户优惠券ID</p>"
          },
          {
            "group": "Parameter",
            "type": "int",
            "optional": true,
            "field": "payMethod",
            "description": "<p>支付方式，余额支付时传入</p>"
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
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "transactionId",
            "description": "<p>流水号</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/OrderController.java",
    "groupTitle": "Order"
  },
  {
    "type": "POST",
    "url": "/v1/o/store/calc_order/",
    "title": "12计算下单金额与优惠",
    "name": "calcOrder",
    "group": "Order",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "string",
            "optional": true,
            "field": "description",
            "description": "<p>备注</p>"
          },
          {
            "group": "Parameter",
            "type": "JsonArray",
            "optional": false,
            "field": "list",
            "description": "<p>购买的商品列表</p>"
          },
          {
            "group": "Parameter",
            "type": "long",
            "optional": false,
            "field": "productId",
            "description": "<p>商品ID</p>"
          },
          {
            "group": "Parameter",
            "type": "long",
            "optional": false,
            "field": "skuId",
            "description": "<p>商品skuID,为0时表示只有一个标准的产品，没有sku</p>"
          },
          {
            "group": "Parameter",
            "type": "long",
            "optional": false,
            "field": "amount",
            "description": "<p>购买数量</p>"
          },
          {
            "group": "Parameter",
            "type": "String",
            "optional": true,
            "field": "remark",
            "description": "<p>备注，每个单品的备注，预留</p>"
          },
          {
            "group": "Parameter",
            "type": "long",
            "optional": true,
            "field": "memberCouponId",
            "description": "<p>用户优惠券ID</p>"
          },
          {
            "group": "Parameter",
            "type": "int",
            "optional": true,
            "field": "payMethod",
            "description": "<p>支付方式，余额支付时传入</p>"
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
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "transactionId",
            "description": "<p>流水号</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/store/StoreOrderController.java",
    "groupTitle": "Order"
  },
  {
    "type": "POST",
    "url": "/v1/o/cancel_order/",
    "title": "07取消订单",
    "name": "cancelOrder",
    "group": "Order",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "long",
            "optional": false,
            "field": "orderId",
            "description": "<p>订单Id</p>"
          },
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "operation",
            "description": "<p>为cancel取消</p>"
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
            "description": "<p>40001 参数错误</p>"
          }
        ],
        "Error 40002": [
          {
            "group": "Error 40002",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>40002 交易密码错误</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/OrderController.java",
    "groupTitle": "Order"
  },
  {
    "type": "POST",
    "url": "/v1/o/store/cancel_order/",
    "title": "07取消订单",
    "name": "cancelOrder",
    "group": "Order",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "long",
            "optional": false,
            "field": "orderId",
            "description": "<p>订单Id</p>"
          },
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "operation",
            "description": "<p>为cancel取消</p>"
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
            "description": "<p>40001 参数错误</p>"
          }
        ],
        "Error 40002": [
          {
            "group": "Error 40002",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>40002 交易密码错误</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/store/StoreOrderController.java",
    "groupTitle": "Order"
  },
  {
    "type": "POST",
    "url": "/v1/o/product_orders/:orderId/",
    "title": "04确认收货",
    "name": "confirmOrder",
    "group": "Order",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "location",
            "description": "<p>location</p>"
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
            "description": "<p>40001 参数错误</p>"
          }
        ],
        "Error 40002": [
          {
            "group": "Error 40002",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>40002 只能确认自己的订单</p>"
          }
        ],
        "Error 40003": [
          {
            "group": "Error 40003",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>40003 该商品不存在</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/OrderController.java",
    "groupTitle": "Order"
  },
  {
    "type": "POST",
    "url": "/v1/o/store/product_orders/:orderId/",
    "title": "04确认收货",
    "name": "confirmOrder",
    "group": "Order",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "location",
            "description": "<p>location</p>"
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
            "description": "<p>40001 参数错误</p>"
          }
        ],
        "Error 40002": [
          {
            "group": "Error 40002",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>40002 只能确认自己的订单</p>"
          }
        ],
        "Error 40003": [
          {
            "group": "Error 40003",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>40003 该商品不存在</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/store/StoreOrderController.java",
    "groupTitle": "Order"
  },
  {
    "type": "POST",
    "url": "/v1/o/delete_order/",
    "title": "45删除订单",
    "name": "deleteOrder",
    "group": "Order",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "long",
            "optional": false,
            "field": "orderId",
            "description": "<p>订单Id</p>"
          },
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "operation",
            "description": "<p>为delete删除</p>"
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
            "description": "<p>40001 参数错误</p>"
          }
        ],
        "Error 40002": [
          {
            "group": "Error 40002",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>40002 交易密码错误</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/OrderController.java",
    "groupTitle": "Order"
  },
  {
    "type": "POST",
    "url": "/v1/o/store/delete_order/",
    "title": "17删除订单",
    "name": "deleteOrder",
    "group": "Order",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "long",
            "optional": false,
            "field": "orderId",
            "description": "<p>订单Id</p>"
          },
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "operation",
            "description": "<p>为delete删除</p>"
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
            "description": "<p>40001 参数错误</p>"
          }
        ],
        "Error 40002": [
          {
            "group": "Error 40002",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>40002 交易密码错误</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/store/StoreOrderController.java",
    "groupTitle": "Order"
  },
  {
    "type": "POST",
    "url": "/v1/o/exchange/",
    "title": "43积分兑换",
    "name": "exchange",
    "group": "Order",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "string",
            "optional": true,
            "field": "description",
            "description": "<p>备注</p>"
          },
          {
            "group": "Parameter",
            "type": "long",
            "optional": false,
            "field": "skuId",
            "description": "<p>商品skuID</p>"
          },
          {
            "group": "Parameter",
            "type": "long",
            "optional": false,
            "field": "addressId",
            "description": "<p>地址ID</p>"
          },
          {
            "group": "Parameter",
            "type": "long",
            "optional": false,
            "field": "amount",
            "description": "<p>购买数量</p>"
          },
          {
            "group": "Parameter",
            "type": "String",
            "optional": true,
            "field": "remark",
            "description": "<p>备注，每个单品的备注，预留</p>"
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
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "transactionId",
            "description": "<p>流水号</p>"
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
            "description": "<p>40002 该收货地址不存在</p>"
          }
        ],
        "Error 40003": [
          {
            "group": "Error 40003",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>40003 该商品不存在</p>"
          }
        ],
        "Error 40004": [
          {
            "group": "Error 40004",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>40004 正在下单中,请稍等</p>"
          }
        ],
        "Error 40005": [
          {
            "group": "Error 40005",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>40005 余额不足,请先充值</p>"
          }
        ],
        "Error 40006": [
          {
            "group": "Error 40006",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>40005 该商品已售完</p>"
          }
        ],
        "Error 40007": [
          {
            "group": "Error 40007",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>40007 库存只剩下*件</p>"
          }
        ],
        "Error 40009": [
          {
            "group": "Error 40009",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>40009 交易密码错误</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/OrderController.java",
    "groupTitle": "Order"
  },
  {
    "type": "GET",
    "url": "/v1/o/express_query/?deliveryNo=&phoneNumber=",
    "title": "46物流查询",
    "name": "expressQuery",
    "group": "Order",
    "description": "<p>{&quot;status&quot;:0,&quot;msg&quot;:&quot;ok&quot;,&quot;result&quot;:{&quot;number&quot;:&quot;SF1407021096548&quot;,&quot;type&quot;:&quot;sfexpress&quot;,&quot;typename&quot;:&quot;顺丰速运&quot;,&quot;logo&quot;:&quot;https://api.jisuapi.com/express/static/images/logo/80/sfexpress.png&quot;,&quot;list&quot;:[{&quot;time&quot;:&quot;2021-05-19 11:33:19&quot;,&quot;status&quot;:&quot;在官网&quot;运单资料&amp;签收图&quot;,可查看签收人信息&quot;},{&quot;time&quot;:&quot;2021-05-19 11:33:18&quot;,&quot;status&quot;:&quot;您的快件已签收，如有疑问请电联快递员【王诗洁，电话：13695058037】。疫情期间顺丰每日对网点消毒、快递员每日测温、配戴口罩，感谢您使用顺丰，期待再次为您服务。（主单总件数：1件）&quot;},{&quot;time&quot;:&quot;2021-05-19 09:03:02&quot;,&quot;status&quot;:&quot;快件交给王诗洁,正在派送途中（联系电话：13695058037,顺丰已开启“安全呼叫”保护您的电话隐私,请放心接听！）（主单总件数：1件）&quot;},{&quot;time&quot;:&quot;2021-05-19 08:33:04&quot;,&quot;status&quot;:&quot;正在派送途中,请您准备签收(派件人:王诗洁,电话:13695058037)&quot;},{&quot;time&quot;:&quot;2021-05-19 07:46:23&quot;,&quot;status&quot;:&quot;快件到达 【南平邵武元隆大厦营业点】&quot;},{&quot;time&quot;:&quot;2021-05-19 05:20:59&quot;,&quot;status&quot;:&quot;快件已发车&quot;},{&quot;time&quot;:&quot;2021-05-19 05:20:42&quot;,&quot;status&quot;:&quot;快件在【南平延平集散点】完成分拣,准备发往 【南平邵武元隆大厦营业点】&quot;},{&quot;time&quot;:&quot;2021-05-19 04:09:11&quot;,&quot;status&quot;:&quot;快件到达 【南平延平集散点】&quot;},{&quot;time&quot;:&quot;2021-05-19 01:19:15&quot;,&quot;status&quot;:&quot;快件已发车&quot;},{&quot;time&quot;:&quot;2021-05-19 00:53:13&quot;,&quot;status&quot;:&quot;快件在【福州兰圃中转场】完成分拣,准备发往 【南平延平集散点】&quot;},{&quot;time&quot;:&quot;2021-05-18 22:18:29&quot;,&quot;status&quot;:&quot;快件已发车&quot;},{&quot;time&quot;:&quot;2021-05-18 22:09:00&quot;,&quot;status&quot;:&quot;快件在【阳下速运营业点】完成分拣,准备发往 【福州兰圃中转场】&quot;},{&quot;time&quot;:&quot;2021-05-18 19:35:38&quot;,&quot;status&quot;:&quot;顺丰速运 已收取快件&quot;},{&quot;time&quot;:&quot;2021-05-18 19:11:40&quot;,&quot;status&quot;:&quot;顺丰速运 已收取快件&quot;}],&quot;deliverystatus&quot;:3,&quot;issign&quot;:1},&quot;code&quot;:200}</p>",
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
            "description": "<p>40001 参数错误</p>"
          }
        ],
        "Error 40002": [
          {
            "group": "Error 40002",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>40002 只能确认自己的订单</p>"
          }
        ],
        "Error 40003": [
          {
            "group": "Error 40003",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>40003 该商品不存在</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/KDController.java",
    "groupTitle": "Order"
  },
  {
    "type": "GET",
    "url": "/v1/o/orders/:orderId/",
    "title": "05订单详情",
    "name": "getOrder",
    "group": "Order",
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
            "type": "String",
            "optional": false,
            "field": "orderNo",
            "description": "<p>订单编号</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "totalMoney",
            "description": "<p>总金额</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "realPay",
            "description": "<p>实付</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "logisticsFee",
            "description": "<p>运费</p>"
          },
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "productCount",
            "description": "<p>商品数量</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "statusName",
            "description": "<p>状态名字</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "description",
            "description": "<p>描述</p>"
          },
          {
            "group": "Success 200",
            "type": "JsonArray",
            "optional": false,
            "field": "detailList",
            "description": "<p>商品列表</p>"
          },
          {
            "group": "Success 200",
            "type": "string",
            "optional": false,
            "field": "productName",
            "description": "<p>商品名字</p>"
          },
          {
            "group": "Success 200",
            "type": "string",
            "optional": false,
            "field": "productImgUrl",
            "description": "<p>商品图片</p>"
          },
          {
            "group": "Success 200",
            "type": "string",
            "optional": false,
            "field": "skuName",
            "description": "<p>商品属性描述</p>"
          },
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "state",
            "description": "<p>售后状态 0 未发起售后 1 申请售后 -1 售后已取消 2 处理中 200 处理完毕</p>"
          },
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "status",
            "description": "<p>0未付款,1已付款,2已发货,3已签收,-1退货申请,-2退货中,-3已退货,-4取消交易 -5撤销申请</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "isMix",
            "description": "<p>是否混搭</p>"
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
            "description": "<p>40002 找不到该订单</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/OrderController.java",
    "groupTitle": "Order"
  },
  {
    "type": "GET",
    "url": "/v1/o/store/orders/:orderId/",
    "title": "05订单详情",
    "name": "getOrder",
    "group": "Order",
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
            "type": "String",
            "optional": false,
            "field": "orderNo",
            "description": "<p>订单编号</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "totalMoney",
            "description": "<p>总金额</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "realPay",
            "description": "<p>实付</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "logisticsFee",
            "description": "<p>运费</p>"
          },
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "productCount",
            "description": "<p>商品数量</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "statusName",
            "description": "<p>状态名字</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "description",
            "description": "<p>描述</p>"
          },
          {
            "group": "Success 200",
            "type": "JsonArray",
            "optional": false,
            "field": "detailList",
            "description": "<p>商品列表</p>"
          },
          {
            "group": "Success 200",
            "type": "string",
            "optional": false,
            "field": "productName",
            "description": "<p>商品名字</p>"
          },
          {
            "group": "Success 200",
            "type": "string",
            "optional": false,
            "field": "productImgUrl",
            "description": "<p>商品图片</p>"
          },
          {
            "group": "Success 200",
            "type": "string",
            "optional": false,
            "field": "skuName",
            "description": "<p>商品属性描述</p>"
          },
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "state",
            "description": "<p>售后状态 0 未发起售后 1 申请售后 -1 售后已取消 2 处理中 200 处理完毕</p>"
          },
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "status",
            "description": "<p>0未付款,1已付款,2已发货,3已签收,-1退货申请,-2退货中,-3已退货,-4取消交易 -5撤销申请</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "isMix",
            "description": "<p>是否混搭</p>"
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
            "description": "<p>40002 找不到该订单</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/store/StoreOrderController.java",
    "groupTitle": "Order"
  },
  {
    "type": "GET",
    "url": "/v1/o/order_pay_result/:orderId/",
    "title": "06查询订单状态",
    "name": "getPayResult",
    "group": "Order",
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
            "field": "status",
            "description": "<p>支付状态</p>"
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
            "description": "<p>40002 找不到该订单</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/OrderController.java",
    "groupTitle": "Order"
  },
  {
    "type": "GET",
    "url": "/v1/o/order_count/",
    "title": "18分类订单数量",
    "name": "listMyOrderCount",
    "group": "Order",
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "unpayOrders",
            "description": "<p>未付款订单数</p>"
          },
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "paidOrders",
            "description": "<p>待发货订单数</p>"
          },
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "delieveredOrders",
            "description": "<p>待收货订单数</p>"
          },
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "toCommentOrders",
            "description": "<p>待评价订单数</p>"
          },
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "postServiceOrders",
            "description": "<p>售后订单数</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/OrderController.java",
    "groupTitle": "Order"
  },
  {
    "type": "GET",
    "url": "/v1/o/store/order_count/",
    "title": "13分类订单数量",
    "name": "listMyOrderCount",
    "group": "Order",
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "unpayOrders",
            "description": "<p>未付款订单数</p>"
          },
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "paidOrders",
            "description": "<p>待发货订单数</p>"
          },
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "delieveredOrders",
            "description": "<p>待收货订单数</p>"
          },
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "toCommentOrders",
            "description": "<p>待评价订单数</p>"
          },
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "postServiceOrders",
            "description": "<p>售后订单数</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/store/StoreOrderController.java",
    "groupTitle": "Order"
  },
  {
    "type": "POST",
    "url": "/v1/o/orders/",
    "title": "01我的订单列表",
    "name": "listMyOrders",
    "group": "Order",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "optional": false,
            "field": "status",
            "description": "<p>0全部，1待付款 3待发货 5待收货 7待评价 9已评价 -1退货申请,-2退货中,-3已退货,-4取消交易 -5撤销申请 -6系统取消 -7退款 -8支付超时关闭</p>"
          },
          {
            "group": "Parameter",
            "optional": false,
            "field": "page",
            "description": "<p>page</p>"
          },
          {
            "group": "Parameter",
            "optional": false,
            "field": "size",
            "description": "<p>size</p>"
          },
          {
            "group": "Parameter",
            "optional": false,
            "field": "orderType",
            "description": "<p>orderType 订单类型</p>"
          },
          {
            "group": "Parameter",
            "optional": false,
            "field": "deliveryType",
            "description": "<p>deliveryType 配送类型</p>"
          },
          {
            "group": "Parameter",
            "optional": false,
            "field": "filter",
            "description": "<p>订单关键字</p>"
          },
          {
            "group": "Parameter",
            "optional": false,
            "field": "productName",
            "description": "<p>商品名字</p>"
          },
          {
            "group": "Parameter",
            "optional": false,
            "field": "payBeingTime",
            "description": "<p>付款开始时间</p>"
          },
          {
            "group": "Parameter",
            "optional": false,
            "field": "payEndTime",
            "description": "<p>付款结束时间</p>"
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
            "field": "pages",
            "description": "<p>页数</p>"
          },
          {
            "group": "Success 200",
            "type": "JsonArray",
            "optional": false,
            "field": "list",
            "description": "<p>订单列表</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "orderNo",
            "description": "<p>订单编号</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "totalMoney",
            "description": "<p>总金额</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "realPay",
            "description": "<p>实付</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "logisticsFee",
            "description": "<p>运费</p>"
          },
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "productCount",
            "description": "<p>商品数量</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "statusName",
            "description": "<p>状态名字</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "description",
            "description": "<p>描述</p>"
          },
          {
            "group": "Success 200",
            "type": "JsonArray",
            "optional": false,
            "field": "detailList",
            "description": "<p>商品列表</p>"
          },
          {
            "group": "Success 200",
            "type": "string",
            "optional": false,
            "field": "productName",
            "description": "<p>商品名字</p>"
          },
          {
            "group": "Success 200",
            "type": "string",
            "optional": false,
            "field": "productImgUrl",
            "description": "<p>商品图片</p>"
          },
          {
            "group": "Success 200",
            "type": "string",
            "optional": false,
            "field": "skuName",
            "description": "<p>商品属性描述</p>"
          },
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "status",
            "description": "<p>状态</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/OrderController.java",
    "groupTitle": "Order"
  },
  {
    "type": "POST",
    "url": "/v1/o/store/orders/",
    "title": "01我的订单列表",
    "name": "listMyOrders",
    "group": "Order",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "optional": false,
            "field": "status",
            "description": "<p>0全部，1待付款 3待发货 5待收货 7待评价 9已评价 -1退货申请,-2退货中,-3已退货,-4取消交易 -5撤销申请 -6系统取消 -7退款 -8支付超时关闭</p>"
          },
          {
            "group": "Parameter",
            "optional": false,
            "field": "page",
            "description": "<p>page</p>"
          },
          {
            "group": "Parameter",
            "optional": false,
            "field": "size",
            "description": "<p>size</p>"
          },
          {
            "group": "Parameter",
            "optional": false,
            "field": "orderType",
            "description": "<p>orderType 订单类型</p>"
          },
          {
            "group": "Parameter",
            "optional": false,
            "field": "deliveryType",
            "description": "<p>deliveryType 配送类型</p>"
          },
          {
            "group": "Parameter",
            "optional": false,
            "field": "filter",
            "description": "<p>订单关键字</p>"
          },
          {
            "group": "Parameter",
            "optional": false,
            "field": "productName",
            "description": "<p>商品名字</p>"
          },
          {
            "group": "Parameter",
            "optional": false,
            "field": "payBeingTime",
            "description": "<p>付款开始时间</p>"
          },
          {
            "group": "Parameter",
            "optional": false,
            "field": "payEndTime",
            "description": "<p>付款结束时间</p>"
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
            "field": "pages",
            "description": "<p>页数</p>"
          },
          {
            "group": "Success 200",
            "type": "JsonArray",
            "optional": false,
            "field": "list",
            "description": "<p>订单列表</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "orderNo",
            "description": "<p>订单编号</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "totalMoney",
            "description": "<p>总金额</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "realPay",
            "description": "<p>实付</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "logisticsFee",
            "description": "<p>运费</p>"
          },
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "productCount",
            "description": "<p>商品数量</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "statusName",
            "description": "<p>状态名字</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "description",
            "description": "<p>描述</p>"
          },
          {
            "group": "Success 200",
            "type": "JsonArray",
            "optional": false,
            "field": "detailList",
            "description": "<p>商品列表</p>"
          },
          {
            "group": "Success 200",
            "type": "string",
            "optional": false,
            "field": "productName",
            "description": "<p>商品名字</p>"
          },
          {
            "group": "Success 200",
            "type": "string",
            "optional": false,
            "field": "productImgUrl",
            "description": "<p>商品图片</p>"
          },
          {
            "group": "Success 200",
            "type": "string",
            "optional": false,
            "field": "skuName",
            "description": "<p>商品属性描述</p>"
          },
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "status",
            "description": "<p>状态</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/store/StoreOrderController.java",
    "groupTitle": "Order"
  },
  {
    "type": "GET",
    "url": "/v1/o/order_stat/",
    "title": "09订单数统计",
    "name": "listOrderStat",
    "group": "Order",
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
            "field": "unpaySize",
            "description": "<p>待支付订单数</p>"
          },
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "toMailSize",
            "description": "<p>待发货订单数</p>"
          },
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "toConfirmSize",
            "description": "<p>待收货订单数</p>"
          },
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "toCommentSize",
            "description": "<p>待评论订单数</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/OrderController.java",
    "groupTitle": "Order"
  },
  {
    "type": "GET",
    "url": "/v1/o/store/order_stat/",
    "title": "08订单数统计",
    "name": "listOrderStat",
    "group": "Order",
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
            "field": "unpaySize",
            "description": "<p>待支付订单数</p>"
          },
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "toMailSize",
            "description": "<p>待发货订单数</p>"
          },
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "toConfirmSize",
            "description": "<p>待收货订单数</p>"
          },
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "toCommentSize",
            "description": "<p>待评论订单数</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/store/StoreOrderController.java",
    "groupTitle": "Order"
  },
  {
    "type": "GET",
    "url": "/v1/o/shop_orders/?page=&status=",
    "title": "28店铺订单列表",
    "name": "listShopOrders",
    "group": "Order",
    "success": {
      "fields": {
        "Success 200": [
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
            "description": "<p>订单列表</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "orderNo",
            "description": "<p>订单编号</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "totalMoney",
            "description": "<p>总金额</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "realPay",
            "description": "<p>实付</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "logisticsFee",
            "description": "<p>运费</p>"
          },
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "productCount",
            "description": "<p>商品数量</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "statusName",
            "description": "<p>状态名字</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "description",
            "description": "<p>描述</p>"
          },
          {
            "group": "Success 200",
            "type": "JsonArray",
            "optional": false,
            "field": "detailList",
            "description": "<p>商品列表</p>"
          },
          {
            "group": "Success 200",
            "type": "string",
            "optional": false,
            "field": "productName",
            "description": "<p>商品名字</p>"
          },
          {
            "group": "Success 200",
            "type": "string",
            "optional": false,
            "field": "productImgUrl",
            "description": "<p>商品图片</p>"
          },
          {
            "group": "Success 200",
            "type": "string",
            "optional": false,
            "field": "skuName",
            "description": "<p>商品属性描述</p>"
          },
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "status",
            "description": "<p>状态</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/OrderController.java",
    "groupTitle": "Order"
  },
  {
    "type": "GET",
    "url": "/v1/o/store/store_orders/?page=&status=",
    "title": "14店铺订单列表",
    "name": "listStoreOrders",
    "group": "Order",
    "success": {
      "fields": {
        "Success 200": [
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
            "description": "<p>订单列表</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "orderNo",
            "description": "<p>订单编号</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "totalMoney",
            "description": "<p>总金额</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "realPay",
            "description": "<p>实付</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "logisticsFee",
            "description": "<p>运费</p>"
          },
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "productCount",
            "description": "<p>商品数量</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "statusName",
            "description": "<p>状态名字</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "description",
            "description": "<p>描述</p>"
          },
          {
            "group": "Success 200",
            "type": "JsonArray",
            "optional": false,
            "field": "detailList",
            "description": "<p>商品列表</p>"
          },
          {
            "group": "Success 200",
            "type": "string",
            "optional": false,
            "field": "productName",
            "description": "<p>商品名字</p>"
          },
          {
            "group": "Success 200",
            "type": "string",
            "optional": false,
            "field": "productImgUrl",
            "description": "<p>商品图片</p>"
          },
          {
            "group": "Success 200",
            "type": "string",
            "optional": false,
            "field": "skuName",
            "description": "<p>商品属性描述</p>"
          },
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "status",
            "description": "<p>状态</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/store/StoreOrderController.java",
    "groupTitle": "Order"
  },
  {
    "type": "POST",
    "url": "/v1/o/pay_order/",
    "title": "03支付订单",
    "name": "payOrder",
    "group": "Order",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "long",
            "optional": false,
            "field": "orderId",
            "description": "<p>订单ID</p>"
          },
          {
            "group": "Parameter",
            "type": "int",
            "optional": false,
            "field": "payMethod",
            "description": "<p>1银行卡支付2支付宝支付3微信支付4余额支付6微信支付H5 7支付宝H5支付8小程序支付 10储值卡</p>"
          },
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "openId",
            "description": "<p>微信公众号支付时需要传入</p>"
          },
          {
            "group": "Parameter",
            "type": "String",
            "optional": true,
            "field": "password",
            "description": "<p>余额支付时需要密码，这个密码为登录密码</p>"
          },
          {
            "group": "Parameter",
            "type": "String",
            "optional": true,
            "field": "vcode",
            "description": "<p>余额支付时需要短信验证码</p>"
          },
          {
            "group": "Parameter",
            "type": "int",
            "optional": true,
            "field": "useScore",
            "description": "<p>0不使用 1使用</p>"
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
          },
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "note",
            "description": "<p>如果是支付宝直接跳到支付宝支付的网页，如果是微信支付返回一个待支付的二维码，字段名codeUrl</p>"
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
            "description": "<p>40002 该订单不存在</p>"
          }
        ],
        "Error 40003": [
          {
            "group": "Error 40003",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>40003 该订单不是待支付状态,不能支付</p>"
          }
        ],
        "Error 40004": [
          {
            "group": "Error 40004",
            "type": "int",
            "optional": false,
            "field": "errCodeDesc",
            "description": "<p>微信支付错误提示</p>"
          }
        ],
        "Error 40005": [
          {
            "group": "Error 40005",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>40005 请输入短信验证码</p>"
          }
        ],
        "Error 40006": [
          {
            "group": "Error 40006",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>40006 余额不足</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/OrderController.java",
    "groupTitle": "Order"
  },
  {
    "type": "POST",
    "url": "/v1/o/store/pay_order/",
    "title": "03支付订单",
    "name": "payOrder",
    "group": "Order",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "long",
            "optional": false,
            "field": "orderId",
            "description": "<p>订单ID</p>"
          },
          {
            "group": "Parameter",
            "type": "int",
            "optional": false,
            "field": "payMethod",
            "description": "<p>1银行卡支付2支付宝支付3微信支付4余额支付6微信支付H5 7支付宝H5支付8小程序支付 10储值卡</p>"
          },
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "openId",
            "description": "<p>微信公众号支付时需要传入</p>"
          },
          {
            "group": "Parameter",
            "type": "String",
            "optional": true,
            "field": "password",
            "description": "<p>余额支付时需要密码，这个密码为登录密码</p>"
          },
          {
            "group": "Parameter",
            "type": "String",
            "optional": true,
            "field": "vcode",
            "description": "<p>余额支付时需要短信验证码</p>"
          },
          {
            "group": "Parameter",
            "type": "int",
            "optional": true,
            "field": "useScore",
            "description": "<p>0不使用 1使用</p>"
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
          },
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "note",
            "description": "<p>如果是支付宝直接跳到支付宝支付的网页，如果是微信支付返回一个待支付的二维码，字段名codeUrl</p>"
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
            "description": "<p>40002 该订单不存在</p>"
          }
        ],
        "Error 40003": [
          {
            "group": "Error 40003",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>40003 该订单不是待支付状态,不能支付</p>"
          }
        ],
        "Error 40004": [
          {
            "group": "Error 40004",
            "type": "int",
            "optional": false,
            "field": "errCodeDesc",
            "description": "<p>微信支付错误提示</p>"
          }
        ],
        "Error 40005": [
          {
            "group": "Error 40005",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>40005 请输入短信验证码</p>"
          }
        ],
        "Error 40006": [
          {
            "group": "Error 40006",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>40006 余额不足</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/store/StoreOrderController.java",
    "groupTitle": "Order"
  },
  {
    "type": "POST",
    "url": "/v1/o/orders/new/",
    "title": "02下单",
    "name": "placeOrder",
    "group": "Order",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "long",
            "optional": false,
            "field": "memberCouponId",
            "description": "<p>平台优惠券D</p>"
          },
          {
            "group": "Parameter",
            "type": "long",
            "optional": false,
            "field": "addressId",
            "description": "<p>收货地址ID</p>"
          },
          {
            "group": "Parameter",
            "type": "String",
            "optional": true,
            "field": "dealerCode",
            "description": "<p>推荐人code</p>"
          },
          {
            "group": "Parameter",
            "type": "long",
            "optional": true,
            "field": "dealerUid",
            "description": "<p>推荐人uid 与dealerCode 二者任选一个</p>"
          },
          {
            "group": "Parameter",
            "type": "JsonArray",
            "optional": false,
            "field": "list",
            "description": "<p>列表</p>"
          },
          {
            "group": "Parameter",
            "type": "long",
            "optional": false,
            "field": "shopId",
            "description": "<p>shopId</p>"
          },
          {
            "group": "Parameter",
            "type": "long",
            "optional": false,
            "field": "deliveryMethod",
            "description": "<p>配送方式 1自提 2跑腿 3邮寄</p>"
          },
          {
            "group": "Parameter",
            "type": "String",
            "optional": true,
            "field": "remark",
            "description": "<p>备注，每个单品的备注，预留</p>"
          },
          {
            "group": "Parameter",
            "type": "int",
            "optional": true,
            "field": "payMethod",
            "description": "<p>支付方式，余额支付时传入</p>"
          },
          {
            "group": "Parameter",
            "type": "JsonArray",
            "optional": false,
            "field": "productList",
            "description": "<p>商品列表</p>"
          },
          {
            "group": "Parameter",
            "type": "long",
            "optional": false,
            "field": "productId",
            "description": "<p>商品ID</p>"
          },
          {
            "group": "Parameter",
            "type": "long",
            "optional": false,
            "field": "skuId",
            "description": "<p>商品skuID,为0时表示只有一个标准的产品，没有sku</p>"
          },
          {
            "group": "Parameter",
            "type": "long",
            "optional": false,
            "field": "amount",
            "description": "<p>购买数量</p>"
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
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "transactionId",
            "description": "<p>流水号</p>"
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
            "description": "<p>40002 该收货地址不存在</p>"
          }
        ],
        "Error 40003": [
          {
            "group": "Error 40003",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>40003 该商品不存在</p>"
          }
        ],
        "Error 40004": [
          {
            "group": "Error 40004",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>40004 正在下单中,请稍等</p>"
          }
        ],
        "Error 40005": [
          {
            "group": "Error 40005",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>40005 余额不足,请先充值</p>"
          }
        ],
        "Error 40006": [
          {
            "group": "Error 40006",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>40005 该商品已售完</p>"
          }
        ],
        "Error 40007": [
          {
            "group": "Error 40007",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>40007 库存只剩下*件</p>"
          }
        ],
        "Error 40009": [
          {
            "group": "Error 40009",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>40009 交易密码错误</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/OrderController.java",
    "groupTitle": "Order"
  },
  {
    "type": "POST",
    "url": "/v1/o/store/orders/new/",
    "title": "02下单",
    "name": "placeOrder",
    "group": "Order",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "long",
            "optional": false,
            "field": "addressId",
            "description": "<p>收货地址ID</p>"
          },
          {
            "group": "Parameter",
            "type": "String",
            "optional": true,
            "field": "dealerCode",
            "description": "<p>推荐人code</p>"
          },
          {
            "group": "Parameter",
            "type": "long",
            "optional": true,
            "field": "dealerUid",
            "description": "<p>推荐人uid 与dealerCode 二者任选一个</p>"
          },
          {
            "group": "Parameter",
            "type": "JsonArray",
            "optional": false,
            "field": "list",
            "description": "<p>列表</p>"
          },
          {
            "group": "Parameter",
            "type": "long",
            "optional": false,
            "field": "shopId",
            "description": "<p>shopId</p>"
          },
          {
            "group": "Parameter",
            "type": "long",
            "optional": false,
            "field": "deliveryMethod",
            "description": "<p>配送方式 1自提 2跑腿 3邮寄</p>"
          },
          {
            "group": "Parameter",
            "type": "String",
            "optional": true,
            "field": "remark",
            "description": "<p>备注，每个单品的备注，预留</p>"
          },
          {
            "group": "Parameter",
            "type": "long",
            "optional": true,
            "field": "memberCouponId",
            "description": "<p>用户优惠券ID</p>"
          },
          {
            "group": "Parameter",
            "type": "int",
            "optional": true,
            "field": "payMethod",
            "description": "<p>支付方式，余额支付时传入</p>"
          },
          {
            "group": "Parameter",
            "type": "JsonArray",
            "optional": false,
            "field": "productList",
            "description": "<p>商品列表</p>"
          },
          {
            "group": "Parameter",
            "type": "long",
            "optional": false,
            "field": "productId",
            "description": "<p>商品ID</p>"
          },
          {
            "group": "Parameter",
            "type": "long",
            "optional": false,
            "field": "skuId",
            "description": "<p>商品skuID,为0时表示只有一个标准的产品，没有sku</p>"
          },
          {
            "group": "Parameter",
            "type": "long",
            "optional": false,
            "field": "amount",
            "description": "<p>购买数量</p>"
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
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "transactionId",
            "description": "<p>流水号</p>"
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
            "description": "<p>40002 该收货地址不存在</p>"
          }
        ],
        "Error 40003": [
          {
            "group": "Error 40003",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>40003 该商品不存在</p>"
          }
        ],
        "Error 40004": [
          {
            "group": "Error 40004",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>40004 正在下单中,请稍等</p>"
          }
        ],
        "Error 40005": [
          {
            "group": "Error 40005",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>40005 余额不足,请先充值</p>"
          }
        ],
        "Error 40006": [
          {
            "group": "Error 40006",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>40005 该商品已售完</p>"
          }
        ],
        "Error 40007": [
          {
            "group": "Error 40007",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>40007 库存只剩下*件</p>"
          }
        ],
        "Error 40009": [
          {
            "group": "Error 40009",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>40009 交易密码错误</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/store/StoreOrderController.java",
    "groupTitle": "Order"
  },
  {
    "type": "POST",
    "url": "/v1/o/order_delievered/",
    "title": "44设置订单已发货",
    "name": "setOrderDelivered",
    "group": "Order",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "long",
            "optional": false,
            "field": "orderId",
            "description": "<p>单号</p>"
          },
          {
            "group": "Parameter",
            "type": "String",
            "optional": true,
            "field": "expressNo",
            "description": "<p>物流单号</p>"
          },
          {
            "group": "Parameter",
            "type": "String",
            "optional": true,
            "field": "expressCompany",
            "description": "<p>物流公司</p>"
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
            "description": "<p>40001 参数错误</p>"
          }
        ],
        "Error 40002": [
          {
            "group": "Error 40002",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>40002 找不到该订单</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/OrderController.java",
    "groupTitle": "Order"
  },
  {
    "type": "POST",
    "url": "/v1/o/store/order_delievered/",
    "title": "16设置订单已发货",
    "name": "setOrderDelivered",
    "group": "Order",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "long",
            "optional": false,
            "field": "orderId",
            "description": "<p>单号</p>"
          },
          {
            "group": "Parameter",
            "type": "String",
            "optional": true,
            "field": "expressNo",
            "description": "<p>物流单号</p>"
          },
          {
            "group": "Parameter",
            "type": "String",
            "optional": true,
            "field": "expressCompany",
            "description": "<p>物流公司</p>"
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
            "description": "<p>40001 参数错误</p>"
          }
        ],
        "Error 40002": [
          {
            "group": "Error 40002",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>40002 找不到该订单</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/store/StoreOrderController.java",
    "groupTitle": "Order"
  },
  {
    "type": "GET",
    "url": "/v1/o/shop_stat/",
    "title": "42店铺统计",
    "name": "shopStat",
    "group": "Order",
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
            "field": "regCount",
            "description": "<p>今日注册人数</p>"
          },
          {
            "group": "Success 200",
            "type": "long",
            "optional": false,
            "field": "orders",
            "description": "<p>今日订单数</p>"
          },
          {
            "group": "Success 200",
            "type": "long",
            "optional": false,
            "field": "totalMoney",
            "description": "<p>今日订单总额</p>"
          },
          {
            "group": "Success 200",
            "type": "long",
            "optional": false,
            "field": "products",
            "description": "<p>今日售出商品数</p>"
          },
          {
            "group": "Success 200",
            "type": "JsonArray",
            "optional": false,
            "field": "list",
            "description": "<p>最新30条统计数据</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/OrderController.java",
    "groupTitle": "Order"
  },
  {
    "type": "POST",
    "url": "/v1/o/subscribe_miniapp_msg/",
    "title": "29订阅小程序通知",
    "name": "subscribeWeappMsg",
    "group": "Order",
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
    "filename": "app/controllers/OrderController.java",
    "groupTitle": "Order"
  },
  {
    "type": "POST",
    "url": "/v1/o/change_order_address/",
    "title": "39修改订单地址",
    "name": "updateOrderContactAddress",
    "group": "Order",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "long",
            "optional": false,
            "field": "orderId",
            "description": "<p>orderId</p>"
          },
          {
            "group": "Parameter",
            "type": "long",
            "optional": false,
            "field": "addressId",
            "description": "<p>addressId</p>"
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
    "filename": "app/controllers/OrderController.java",
    "groupTitle": "Order"
  },
  {
    "type": "POST",
    "url": "/v1/o/store/change_order_address/",
    "title": "15修改订单地址",
    "name": "updateOrderContactAddress",
    "group": "Order",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "long",
            "optional": false,
            "field": "orderId",
            "description": "<p>orderId</p>"
          },
          {
            "group": "Parameter",
            "type": "long",
            "optional": false,
            "field": "addressId",
            "description": "<p>addressId</p>"
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
    "filename": "app/controllers/store/StoreOrderController.java",
    "groupTitle": "Order"
  },
  {
    "type": "POST",
    "url": "/v1/o/order_return/:id/",
    "title": "11申请售后修改",
    "name": "updateOrderReturns",
    "group": "Order",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "reason",
            "description": "<p>理由</p>"
          },
          {
            "group": "Parameter",
            "type": "long",
            "optional": false,
            "field": "returnMoney",
            "description": "<p>退款金额</p>"
          },
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "note",
            "description": "<p>备注</p>"
          },
          {
            "group": "Parameter",
            "type": "Array",
            "optional": true,
            "field": "imgList",
            "description": "<p>图片地址的数组</p>"
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
    "filename": "app/controllers/OrderController.java",
    "groupTitle": "Order"
  },
  {
    "type": "POST",
    "url": "/v1/o/store/order_return/:id/",
    "title": "11申请售后修改",
    "name": "updateOrderReturns",
    "group": "Order",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "reason",
            "description": "<p>理由</p>"
          },
          {
            "group": "Parameter",
            "type": "long",
            "optional": false,
            "field": "returnMoney",
            "description": "<p>退款金额</p>"
          },
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "note",
            "description": "<p>备注</p>"
          },
          {
            "group": "Parameter",
            "type": "Array",
            "optional": true,
            "field": "imgList",
            "description": "<p>图片地址的数组</p>"
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
    "filename": "app/controllers/store/StoreOrderController.java",
    "groupTitle": "Order"
  },
  {
    "type": "POST",
    "url": "/v1/alipay_notify/",
    "title": "01支付宝回调接口",
    "name": "handlePagePayNotify",
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
        ],
        "Error 40008": [
          {
            "group": "Error 40008",
            "type": "int",
            "optional": false,
            "field": "code",
            "description": "<p>40008 交易密码错误</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/AliPayController.java",
    "groupTitle": "Pay"
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
    "url": "/v1/alipay_verify/",
    "title": "02支付宝回调验签接口",
    "name": "verifySign",
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
          },
          {
            "group": "Success 200",
            "type": "boolean",
            "optional": false,
            "field": "result",
            "description": "<p>true为正确签名，false为假签名</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "app/controllers/AliPayController.java",
    "groupTitle": "Pay"
  }
] });
