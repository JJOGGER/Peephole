package com.bairuitech.anychat;

/**
 * Created by jogger on 2018/5/10.
 */

public class AnychatErrorCode {

    // GV_ERR_SUCCESS				0		///< 成功
    // AC_ERROR_SUCCESS			0		///< no error


// system error code define
    // AC_ERROR_DB_ERROR			1		///< 数据库错误
    // AC_ERROR_NOTINIT			2		///< 系统没有初始化
    //	AC_ERROR_NOTINROOM			3		///< 还未进入房间
    // AC_ERROR_MEMORYFAIL			4       ///< not enough memory
    // AC_ERROR_EXCEPTION			5		///< 出现异常
    // AC_ERROR_CANCEL				6		///< 操作被取消
    // AC_ERROR_PROTOCOLFAIL		7		///< 通信协议出错
    // AC_ERROR_SESSIONNOTEXIST	8		///< 会话不存在
    // AC_ERROR_DATANOTEXIST		9		///< 数据不存在
    // AC_ERROR_DATAEXIST			10		///< 数据已经存在
    // AC_ERROR_INVALIDGUID		11		///< 无效GUID
    // AC_ERROR_RESOURCERECOVER	12		///< 资源被回收
    // AC_ERROR_RESOURCEUSED		13		///< 资源被占用
    // AC_ERROR_JSONFAIL			14		///< Json解析出错
    // AC_ERROR_OBJECTDELETE		15		///< 对象被删除
    // AC_ERROR_SESSIONEXIST		16		///< 会话已存在
    // AC_ERROR_SESSIONNOTINIT		17		///< 会话没有初始化

    // AC_ERROR_FUNCNOTALLOW		20		///< 函数功能不允许
    // AC_ERROR_FUNCOPTERROR       21      ///< function parameters error
    // AC_ERROR_DEVICEOPENFAIL     22      ///< device open failed or device no install
    // AC_ERROR_NOENOUGHRESOURCE	23		///< 没有足够的资源
    // AC_ERROR_PIXFMTNOTSUPPORT	24		///< 指定的格式不能被显示设备所支持
    // AC_ERROR_NOTMULTICASTADDR	25		///< 指定的IP地址不是有效的组播地址
    // AC_ERROR_MULTIRUNERROR		26		///< 不支持多实例运行
    // AC_ERROR_FILETRUSTFAILED	27		///< 文件签名验证失败
    // AC_ERROR_CERTVERIFYFAILED	28		///< 授权验证失败
    // AC_ERROR_CERTUSERFAILED		29		///< 授权证书用户数验证失败
    // AC_ERROR_MASTERISSLAVE		30		///< 所指定的主服务器是热备服务器，不支持再次热备
    // AC_ERROR_MASTERNOTCREDIT	31		///< 主服务器没有经过授权认证，不支持热备
    // AC_ERROR_VERSIONNOTMATCH	32		///< 版本不匹配
    // AC_ERROR_CERTFAILSECOND		33		///< 第二次授权验证失败
    // AC_ERROR_SERVERVERIFYFAIL	34		///< 服务器安全验证失败
    // AC_ERROR_CLIENTCERTFAILED	35		///< 客户端授权验证失败
    // AC_ERROR_CERTSUMFAILED		36		///< 授权功能校验失败
    // AC_ERROR_REMOTECTRL			37		///< 远程控制
    // AC_ERROR_DUPLICATESERVICEID	38		///< ServiceGuid重复
    // AC_ERROR_DIRENTERROR		39		///< 目录错误
    // AC_ERROR_EXTRACTFILEERROR	40		///< 解压文件失败
    // AC_ERROR_STARTPROCESSFAILED	41		///< 启动进程失败
    // AC_ERROR_SERVICEISRUNNING	42		///< 服务已启动
    // AC_ERROR_DISKSPACELIMITED	43		///< 磁盘空间不足
    // AC_ERROR_REQUESTFAILED		44		///< 业务服务发送请求失败
    // AC_ERROR_INVALIDMACHINE		45		///< 无效的物理机对象
    // AC_ERROR_GETCERTINFOFAILED	46		///< 获取授权信息失败
    // AC_ERROR_CLUSTERNOTMATCH	47		///< 集群属性不匹配
    // AC_ERROR_NONECLUSTERID		48		///< 集群ID为空
    // AC_ERROR_CREATESERVICE_MORE	49		///< 同台物理机创建多个相同服务，一类服务暂时不允许创建多个
    // AC_ERROR_COPYFILEFAILED		50		///< 拷贝文件失败
    // AC_ERROR_CLOUDNATIVEDBFAIL	51		///< 云平台内部数据库出错
    // AC_ERROR_CLOUDOSSUPLOADFAIL	52		///< 云平台OSS文件上传失败
    // AC_ERROR_SERVICEBINDCHANGE	53		///< 服务绑定关系变化
    // AC_ERROR_SERVICENOTBIND		54		///< 服务没有被绑定
    // AC_ERROR_SERVICEBINDFAIL	55		///< 服务绑定失败
    // AC_ERROR_PIPELINEUSERFAIL	56		///< PipeLine通信用户ID出错
    // AC_ERROR_PIPELINESESSFAIL	57		///< PipeLine通信会话出错
    // AC_ERROR_SERVICECLOSED		58		///< 服务被关闭

//连接部分
    // AC_ERROR_CONNECT_TIMEOUT	100		///< 连接服务器超时
    // AC_ERROR_CONNECT_ABORT		101		///< 与服务器的连接中断
    // AC_ERROR_CONNECT_AUTHFAIL	102		///< 连接服务器认证失败（服务器设置了认证密码）
    // AC_ERROR_CONNECT_DNSERROR	103		///< 域名解析失败
    // AC_ERROR_CONNECT_OVERFLOW	104		///< 超过授权用户数
    //	AC_ERROR_CONNECT_FUNCLIMIT	105		///< 服务器功能受限制（演示模式）
    //	AC_ERROR_CONNECT_INTRANET	106		///< 只能在内网使用
    //	AC_ERROR_CONNECT_OLDVERSION	107		///< 版本太旧，不允许连接
    // AC_ERROR_CONNECT_SOCKETERR	108		///< Socket出错
    //	AC_ERROR_CONNECT_DEVICELIMIT 109	///< 设备连接限制（没有授权）
    // AC_ERROR_CONNECT_PAUSED		110		///< 服务已被暂停
    // AC_ERROR_CONNECT_HOTSERVER	111		///< 热备服务器不支持连接（主服务在启动状态）
    // AC_ERROR_CONNECT_ERRCERUSER	112		///< 授权用户数校验出错，可能内存被修改
    // AC_ERROR_CONNECT_IPFORBID	113		///< IP被禁止连接
    // AC_ERROR_CONNECT_TYPEWRONG	114		///< 连接类型错误，服务器不支持当前类型的连接
    // AC_ERROR_CONNECT_ERRORIP	115		///< 服务器IP地址不正确
    // AC_ERROR_CONNECT_SELFCLOSE	116		///< 连接被主动关闭
    // AC_ERROR_CONNECT_NOSVRLIST	117		///< 没有获取到服务器列表
    // AC_ERROR_CONNECT_LBTIMEOUT	118		///< 连接负载均衡服务器超时
    // AC_ERROR_CONNECT_NOTWORK	119		///< 服务器不在工作状态
    // AC_ERROR_CONNECT_OFFLINE	120		///< 服务器不在线
    // AC_ERROR_CONNECT_NETLIMITED	121		///< 网络带宽受限
    // AC_ERROR_CONNECT_LOWTRAFFIC	122		///< 网络流量不足
    // AC_ERROR_CONNECT_IPV6FAIL	123		///< 不支持IPv6 Only网络
    // AC_ERROR_CONNECT_NOMASTER	124		///< 没有Master服务器在线
    // AC_ERROR_CONNECT_NOSTATUS	125		///< 没有上报工作状态

//登录部分
    // AC_ERROR_CERTIFY_FAIL		200		///< 认证失败，用户名或密码有误
    // AC_ERROR_ALREADY_LOGIN		201		///< 该用户已登录
    // AC_ERROR_ACCOUNT_LOCK		202		///< 帐户已被暂时锁定
    // AC_ERROR_IPADDR_LOCK		203		///< IP地址已被暂时锁定
    // AC_ERROR_VISITOR_DENY		204		///< 游客登录被禁止（登录时没有输入密码）
    // AC_ERROR_INVALID_USERID		205		///< 无效的用户ID（用户不存在）
    // AC_ERROR_SERVERSDK_FAIL		206		///< 与业务服务器连接失败，认证功能失效
    // AC_ERROR_SERVERSDK_TIMEOUT	207		///< 业务服务器执行任务超时
    // AC_ERROR_NOTLOGIN			208		///< 没有登录
    //	AC_ERROR_LOGIN_NEWLOGIN		209		///< 该用户在其它计算机上登录
    // AC_ERROR_LOGIN_EMPTYNAME	210		///< 用户名为空
    // AC_ERROR_KICKOUT			211		///< 被服务器踢掉
    // AC_ERROR_SERVER_RESTART		212		///< 业务服务器重启
    // AC_ERROR_FORBIDDEN			213		///< 操作被禁止，没有权限
    // AC_ERROR_SIGSTREMPTY		214		///< 签名信息为空，禁止登录
    // AC_ERROR_SIGVERIFYFAIL		215		///< 签名验证失败
    // AC_ERROR_SIGPUBLICKEYEMPTY	216		///< 签名验证公钥为空
    // AC_ERROR_SIGPRIVATEKEYEMPTY	217		///< 签名私钥为空
    // AC_ERROR_SIGPARAMEMPTY		218		///< 签名参数为空
    // AC_ERROR_SIGPARAMFAIL		219		///< 签名参数出错
    // AC_ERROR_SIGTIMEFAILURE		220		///< 签名时间失效
    // AC_ERROR_APPNOTACTIVE		221		///< 应用没有被激活
    // AC_ERROR_APPPAUSED			222		///< 应用被用户暂停
    // AC_ERROR_APPLOCKED			223		///< 应用被用户锁定
    // AC_ERROR_APPEXPIRED			224		///< 应用已过期
    // AC_ERROR_APPUNKNOWSTATUS	225		///< 应用未知状态
    // AC_ERROR_SIGALREADYUSED		226		///< 签名已经被使用
    // AC_ERROR_USERROLE_FAIL		227		///< 获取用户角色失败
    // AC_ERROR_INVALID_AGENT		228		///< 坐席无效(不存在)

//进入房间
    // AC_ERROR_ROOM_LOCK			300		///< 房间已被锁住，禁止进入
    // AC_ERROR_ROOM_PASSERR		301		///< 房间密码错误，禁止进入
    // AC_ERROR_ROOM_FULLUSER		302		///< 房间已满员，不能进入
    // AC_ERROR_ROOM_INVALID		303		///< 房间不存在
    // AC_ERROR_ROOM_EXPIRE		304		///< 房间服务时间已到期
    // AC_ERROR_ROOM_REJECT		305		///< 房主拒绝进入
    // AC_ERROR_ROOM_OWNERBEOUT	306		///< 房主不在，不能进入房间
    // AC_ERROR_ROOM_ENTERFAIL		307		///< 不能进入房间
    // AC_ERROR_ROOM_ALREADIN		308		///< 已经在房间里面了，本次进入房间请求忽略
    // AC_ERROR_ROOM_NOTIN			309		///< 不在房间中，对房间相关的API操作失败

// 数据流
    // AC_ERROR_STREAM_OLDPACK		350		///< 过期数据包
    // AC_ERROR_STREAM_SAMEPAK		351		///< 相同的数据包
    // AC_ERROR_STREAM_PACKLOSS	352		///< 数据包丢失
    // AC_ERROR_STREAM_MISTAKE		353		///< 数据包出错，帧序号存在误差
    // AC_ERROR_STREAM_LACKBUFFER	354		///< 媒体流缓冲时间不足

//私聊
    // AC_ERROR_ROOM_PRINULL		401		///< 用户已经离开房间
    // AC_ERROR_ROOM_REJECTPRI		402		///< 用户拒绝了私聊邀请
    // AC_ERROR_ROOM_PRIDENY		403		///< 不允许与该用户私聊，或是用户禁止私聊

    // AC_ERROR_ROOM_PRIREQIDERR	420		///< 私聊请求ID号错误，或请求不存在
    // AC_ERROR_ROOM_PRIALRCHAT	421		///< 已经在私聊列表中

    // AC_ERROR_ROOM_PRITIMEOUT	431		///< 私聊请求超时
    // AC_ERROR_ROOM_PRICHATBUSY	432		///< 对方正在私聊中，繁忙状态
    // AC_ERROR_ROOM_PRIUSERCLOSE	433		///< 对方用户关闭私聊
    // AC_ERROR_ROOM_PRISELFCLOSE	434		///< 用户自己关闭私聊
    // AC_ERROR_ROOM_PRIREQCANCEL	435		///< 私聊请求被取消

// 视频呼叫
    // AC_ERROR_VIDEOCALL_INCHAT	440		///< 正在通话中

//Mic控制权
    //	AC_ERROR_MICLOSE_TIMEOUT	500		///< 说话时间太长，请休息一下
    // AC_ERROR_MICLOSE_HIGHUSER	501		///< 有高级别用户需要发言，请休息一下


// 集群总线
    // AC_ERROR_COMMBUS_SELFMASTER		610	///< 本地总线为Master状态
    // AC_ERROR_COMMBUS_OTHERMASTER	611	///< 有其它总线存在
    // AC_ERROR_COMMBUS_LOWPRIORITY	612 ///< 优先级不够

// 传输部分
    // AC_ERROR_TRANSBUF_CREATEFAIL	700	///< 创建任务失败
    // AC_ERROR_TRANSBUF_NOTASK		701	///< 没有该任务，或是任务已完成

    // AC_ERROR_TRANSFILE_OPENFAIL		710	///< 打开文件出错
    // AC_ERROR_TRANSFILE_ZEROLEN		711	///< 文件长度为0
    // AC_ERROR_TRANSFILE_TOOLARGE		712	///< 文件长度太大
    // AC_ERROR_TRANSFILE_READFAIL		713	///< 读文件出错
    // AC_ERROR_TRANSFILE_DOWNLOADING	714	///< 文件正在下载中
    // AC_ERROR_TRANSFILE_FAILED		715	///< 文件下载失败
    // AC_ERROR_TRANSFILE_NOTASK		716	///< 没有该任务，或是任务已完成

// 录像部分
    // AC_ERROR_RECORD_NOTASK			720	///< 没有录像任务
    // AC_ERROR_RECORD_CREATEFAIL		721	///< 创建录像任务失败
    // AC_ERROR_RECORD_WAITINFO		722	///< 等待用户相关信息，暂时不能录像

// 队列部分
    // AC_ERROR_QUEUE_INVALID			750	///< 无效的队列ID
    // AC_ERROR_QUEUE_PREPARESERVICE	751	///< 准备接受服务，离开队列

// SDK警告
    // AC_ERROR_WARNING_UDPFAIL		780	///< 与服务器的UDP通信异常，流媒体服务将不能正常工作
    // AC_ERROR_WARNING_MISCUTILFAIL	781	///< SDK加载brMiscUtil.dll动态库失败，部分功能将失效
    // AC_ERROR_WARNING_MEDIAUTILFAIL	782	///< SDK加载brMediaUtil.dll动态库失败，部分功能将失效
    // AC_ERROR_WARNING_MEDIACOREFAIL	783	///< SDK加载brMediaCore.dll动态库失败，部分功能将失效
    // AC_ERROR_WARNING_MEDIASHOWFAIL	784	///< SDK加载brMediaShow.dll动态库失败，部分功能将失效
}
