package com.boyitech.logstream.core.util.filter_rule;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.boyitech.logstream.core.cache.BaseCache;
import com.boyitech.logstream.core.info.Event;
import com.boyitech.logstream.core.info.InetInfo;
import com.boyitech.logstream.core.manager.cache.CacheManager;
import com.boyitech.logstream.core.manager.cache.YSCacheManager;

public class FilterRuler {
    //TODO
    //目前过滤规则封装为List，过滤的时候逐条匹配为Event对象设置index值。
    //之后根据需要考虑替换为规则树，规则中每一项为一个节点。
    //这样相同节点可以归并，这样匹配的时候可以在一定程度上避免相同条件多次匹配。

//		private JTree tree;

    private List<FilterRule> rules;
    private CacheManager cacheManager = new YSCacheManager();
    private Map<String,BaseCache> cacheMap = new HashMap(); //<index,cache>

    public FilterRuler(List<FilterRule> rules) {
        this.rules = rules;
        for (FilterRule rule : rules) {
//            BaseCache lv1Cache = cacheManager.getLv1Cache(rule.getLv1CacheID());
//            cacheMap.put(rule.getIndex(),lv1Cache);
        }
    }

    /*
     * @Author Eric Zheng
     * @Description 根据过滤配置来创建Event，为什么要这样设计，
     * 防止大量的非法访问导致系统创建大量的Event对象，尽可能的减少对象的创建
     * @Date 10:11 2019/7/23
     **/
    public Event findIndexForEvent(InetInfo inetInfo) {
        for (FilterRule rule : rules) {
            if (rule.contains(inetInfo)) {
                Event e = new Event();
//                e.setIndex(rule.getIndex());
                return e;
            }
        }
        return null;
    }

    public BaseCache findCacheForEvent(Event e){
        BaseCache baseCache = cacheMap.get(e.getIndex());
        return baseCache;
    }

//		public FilterRuler(List<FilterRule> rules){
//			DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
//			DefaultMutableTreeNode tcp = new DefaultMutableTreeNode(new ProtocolRule("tcp"));
//			DefaultMutableTreeNode udp = new DefaultMutableTreeNode(new ProtocolRule("udp"));
//			for(FilterRule rule : rules){
//				DefaultMutableTreeNode tmp = null;
//				switch(rule.getProtocol()){
//				case "tcp":
//					tmp = tcp;
//					break;
//				case "udp":
//					tmp = udp;
//					break;
//				}
//				DefaultMutableTreeNode srcAddr = new DefaultMutableTreeNode(rule.getSrcAddr());
//				DefaultMutableTreeNode srcPort = new DefaultMutableTreeNode(rule.getSrcPort());
//				DefaultMutableTreeNode dstAddr = new DefaultMutableTreeNode(rule.getDstAddr());
//				DefaultMutableTreeNode dstPort = new DefaultMutableTreeNode(rule.getDstPort());
//				DefaultMutableTreeNode index = new DefaultMutableTreeNode(rule.getIndex());
//				addNode(addNode(addNode(addNode(addNode(tmp, srcAddr), srcPort), dstAddr), dstPort), index);
//			}
//			root.add(tcp);
//			root.add(udp);
//			tree = new JTree(root);
//		}
//
//		private DefaultMutableTreeNode addNode(DefaultMutableTreeNode parent, DefaultMutableTreeNode newChild){
//			if(parent.getChildCount()>0){
//				DefaultMutableTreeNode oldChild = null;
//				boolean flag = false;
//				for(Enumeration<DefaultMutableTreeNode> e = parent.children(); e.hasMoreElements(); ) {
//					oldChild = e.nextElement();
//					FilterRuleInterface oldRule = (FilterRuleInterface) oldChild.getUserObject();
//					FilterRuleInterface newRule = (FilterRuleInterface) newChild.getUserObject();
//					if(oldRule.same(newRule)){
//						flag = true;
//						return oldChild;
//					}
//				}
//				return oldChild;
//			}else{
//				parent.add(newChild);
//				return newChild;
//			}
//		}

}
