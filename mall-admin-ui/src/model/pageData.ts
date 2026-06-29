class PageData {
  pageNumber = 1;

  pageSize = 10;

  // 总数据量
  totalRow = 0;

  // 总页数
  totalPage = 0;

  constructor(
    pageNumber?: number,
    pageSize?: number,
    totalRow?: number,
    totalPage?: number
  ) {
    this.pageNumber = pageNumber || 1;
    this.pageSize = pageSize || 10;
    if (totalRow) this.totalRow = totalRow;
    if (totalPage) this.totalPage = totalPage;
  }

  updatePage(data: any) {
    this.pageNumber = data.pageNumber || 1;
    this.pageSize = data.pageSize || 10;
    if (data.totalRow) this.totalRow = data.totalRow;
    if (data.totalPage) this.totalPage = data.totalPage;
  }

  updatePageData(newPage: PageData): void {
    this.pageSize = newPage.pageSize;
    this.pageNumber = newPage.pageNumber;
    this.totalRow = newPage.totalRow;
    this.totalPage = newPage.totalPage;
  }
}

export default PageData;
