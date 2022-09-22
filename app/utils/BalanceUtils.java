package utils;

import io.ebean.Ebean;
import io.ebean.SqlUpdate;
import models.log.BalanceLog;
import models.msg.Msg;
import models.user.MemberBalance;
import play.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;

import static constants.BusinessConstant.TRANSACTION_TYPE_SUBTRACT_SCORE_GAVE_FOR_CANCEL_ORDER;

/**
 * 余额辅助类
 */
@Singleton
public class BalanceUtils {
    Logger.ALogger logger = Logger.of(BalanceUtils.class);
    @Inject
    DateUtils dateUtils;

    /**
     * 修改用户币种金额表,统一用加的方式,如果需要扣除,则需要对应的参数设为负数
     *
     * @param param
     * @return
     */
    public MemberBalance saveChangeBalance(BalanceParam param, boolean needLog) {
        if (null == param) return null;
        if (ValidationUtil.isEmpty(param.desc)) param.desc = "";
        long currentTime = dateUtils.getCurrentTimeBySecond();

        MemberBalance balance = MemberBalance.find.query().where()
                .eq("uid", param.memberId)
                .eq("itemId", param.itemId)
                .setMaxRows(1).forUpdate().findOne();
        long daysToSeconds = param.days * 24 * 3600;
        if (null == balance) {
            balance = new MemberBalance();
            balance.setUid(param.memberId);
            balance.setItemId(param.itemId);
            balance.setLeftBalance(param.leftBalance);
            balance.setFreezeBalance(param.freezeBalance);
            balance.setTotalBalance(param.totalBalance);
            balance.setUpdateTime(currentTime);
            balance.setExpireTime(currentTime + daysToSeconds);
            balance.setCreatedTime(currentTime);
            balance.save();
        } else {
            String sql = "UPDATE " +
                    "    `v1_member_balance` AS `dest`, " +
                    "    ( SELECT * FROM `v1_member_balance` " +
                    "        WHERE uid = :uid   AND item_id = :itemId limit 1" +
                    "    ) AS `src`" +
                    "   SET" +
                    "       `dest`.`left_balance` = `src`.`left_balance`+ :leftBalance," +
                    "       `dest`.`freeze_balance` = `src`.`freeze_balance`+ :freezeBalance," +
                    "       `dest`.`total_balance` = `src`.`total_balance`+ :totalBalance, " +
                    "       `dest`.`expire_time` = `src`.`expire_time`+ :daysToSecond, " +
                    "       `dest`.`update_time` =   :updateTime   " +
                    "   WHERE" +
                    "   dest.uid = :uid " +
                    "   AND dest.item_id = :itemId " +
                    ";";
            SqlUpdate sqlUpdate = Ebean.createSqlUpdate(sql);
            sqlUpdate.setParameter("uid", param.memberId);
            sqlUpdate.setParameter("itemId", param.itemId);
            sqlUpdate.setParameter("leftBalance", param.leftBalance);
            sqlUpdate.setParameter("freezeBalance", param.freezeBalance);
            sqlUpdate.setParameter("totalBalance", param.totalBalance);
            sqlUpdate.setParameter("updateTime", currentTime);
            sqlUpdate.setParameter("daysToSecond", daysToSeconds);
            sqlUpdate.executeNow();
        }
        if (needLog) insertBalanceLog(balance, param);
        return balance;
    }

    public void insertBalanceLog(MemberBalance balanceParam, BalanceParam param) {
        MemberBalance balance = MemberBalance.find.query().where()
                .eq("uid", balanceParam.uid)
                .eq("itemId", balanceParam.itemId)
                .setMaxRows(1).findOne();
        long currentTime = dateUtils.getCurrentTimeBySecond();
        BalanceLog balanceLog = new BalanceLog();
        balanceLog.setUid(balance.uid);
        balanceLog.setItemId(balance.itemId);
        balanceLog.setLeftBalance(balance.leftBalance);
        balanceLog.setFreezeBalance(balance.freezeBalance);
        balanceLog.setTotalBalance(balance.totalBalance);
        balanceLog.setChangeAmount(param.changeAmount);
        balanceLog.setBizType(param.bizType);
        balanceLog.setNote(param.desc);
        balanceLog.setCreateTime(currentTime);
        balanceLog.setOrderNo(param.orderNo);
        balanceLog.setFreezeStatus(param.freezeStatus);
        balanceLog.save();

        Msg msg = new Msg();
        msg.setUid(balance.uid);
        msg.setTitle(param.desc);
        msg.setContent(param.desc);
        msg.setLinkUrl("");
        msg.setMsgType(Msg.MSG_TYPE_BALANCE);
        msg.setStatus(Msg.STATUS_NOT_READ);
        msg.setItemId(balance.itemId);
        msg.setChangeAmount(param.changeAmount);
        msg.setCreateTime(currentTime);
        msg.save();

    }

}
