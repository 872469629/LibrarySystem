package com.cc.library.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.cc.library.dao.BackDao;
import com.cc.library.dao.BookDao;
import com.cc.library.dao.BorrowDao;
import com.cc.library.domain.BackInfo;
import com.cc.library.domain.Book;
import com.cc.library.domain.BorrowInfo;
import com.cc.library.domain.PageBean;
import com.cc.library.service.BackService;

public class BackServiceImpl implements BackService{

	private BackDao backDao;
	private BookDao bookDao;
	private BorrowDao borrowDao;
	
	
	public void setBookDao(BookDao bookDao) {
		this.bookDao = bookDao;
	}

	public void setBorrowDao(BorrowDao borrowDao) {
		this.borrowDao = borrowDao;
	}

	public void setBackDao(BackDao backDao) {
		this.backDao = backDao;
	}

	@Override
	public PageBean<BackInfo> findBackInfoByPage(int pageCode, int pageSize) {
		// TODO Auto-generated method stub
		return backDao.findBackInfoByPage(pageCode,pageSize);
	}

	@Override
	public BackInfo getBackInfoById(BackInfo backInfo) {
		// TODO Auto-generated method stub
		return backDao.getBackInfoById(backInfo);
	}

	@Override
	public PageBean<BackInfo> queryBackInfo(String iSBN, String paperNO,int pageCode,int pageSize) {
		PageBean<BackInfo> pageBean = new PageBean<BackInfo>();
		pageBean.setPageCode(pageCode);
		pageBean.setPageSize(pageSize);
		PageBean<Integer> list = backDao.getBorrowIdList(iSBN,paperNO,pageCode,pageSize);
		pageBean.setTotalRecord(list.getTotalRecord());
		List<Integer> beanList = list.getBeanList();
		if(beanList.size()==0){
			return null;
		}
		List<BackInfo> backInfos = new ArrayList<BackInfo>();
		for(Integer i : beanList){
			BackInfo backInfo = new BackInfo();
			backInfo.setBorrowId(i);
			BackInfo info = backDao.getBackInfoById(backInfo);
			backInfos.add(info);
		}
		pageBean.setBeanList(backInfos);
		return pageBean;
	}

	@Override
	public int addBackInfo(BackInfo backInfo) {
		//还书的步骤
		/*
		 * 1. 获得操作的借阅编号
		 * 2. 获得当前的管理员
		 * 3. 获得借阅的书籍
		 * 		3.1 书籍的在馆数量增加
		 * 4. 获取当前时间
		 * 5. 设置操作管理员
		 * 6. 设置归还时间
		 * 7. 设置归还的状态
		 * 8. 设置借阅的状态 
		 */
		BorrowInfo borrowInfoById = borrowDao.getBorrowInfoById(backInfo.getBorrowInfo());//获得操作的借阅编号
		if(borrowInfoById.getState()==1){
			return -1;
		}
		Book book = borrowInfoById.getBook();
		Book bookById = bookDao.getBookById(book);//获得借阅的书籍
		bookById.setCurrentNum(bookById.getCurrentNum()+1);
		Book b = bookDao.updateBookInfo(bookById);// 书籍的在馆数量增加
		Date backDate = new Date(System.currentTimeMillis());//获取当前时间
		BackInfo backInfoById = backDao.getBackInfoById(backInfo);
		backInfoById.setAdmin(backInfo.getAdmin());//设置管理员
		backInfoById.setBackDate(backDate);//设置归还时间
		backInfoById.setState(1);///设置归还的状态
		BackInfo ba = null;
		if(b!=null){
			 ba = backDao.updateBackInfo(backInfoById);//修改归还记录
		}
		borrowInfoById.setState(1);//设置借阅的状态
		BorrowInfo bi = null;
		if(ba!=null){
			 bi = borrowDao.updateBorrowInfo(borrowInfoById);
		}
		if(bi!=null){
			return 1;
		}
		return 0;
	}
	
	
}
