######################################################
=+= aldarEconomyModule by camelCase#6543(디스코드) =+=
######################################################


#플러그인 주요 기능: 전체 경제 시스템 관리 및 돈의 흐름 추적

* 돈의 발행과 소모를 편리하게.
 - 기존 이코노미 시스템의 편리성을 그대로 가져왔습니다.
 - 돈 생성을 위한 아이템 정보를 플러그인마다 관리하지 않아도 됩니다.
 - 거래시스템을 따로 구현하지 않아도 됩니다.
 - 서버 내에서 돈이 발행될때와 소멸할때 추적이 가능하도록 돈을 지급해야 하는 상점 모듈 등에서는 EconomyModule을 활용해야 합니다.
 - EconomyModule을 사용하지 않고 돈을 생성할 경우 위조 화폐로 감지하여 사용할 수 없습니다.
  
* 돈이 어디서 발급되었는지 추적 가능.
 - 유저들의 컨텐츠 이용 패턴을 파악할 수 있음.

* 유저들이 돈을 얼마나 소유했는지 알 수 있음
 - 이코노미 플러그인을 쓰지 않고 실물 돈을 사용해도 유저들이 돈을 얼마나 가졌는지 알 수 있음.
 
* 모든 거래 추적 가능
 - 거래량이 많은 지역(섬)이나 거래량이 많은 특정 유저, 유저 그룹, 유저 레벨 등을 추적 가능.
 - 이를 이용할 경우 유저의 플레이 유형에 따른 거래 패턴을 파악하여 맞춤형 컨텐츠를 제작할 수 있음.
 - 인공지능 분석을 활용할 경우 특정 유저의 과거 컨텐츠 이용 패턴에 따른 서버 접속률, 미래 활동 패턴(접는 날짜 등)을 예측 가능.
 - 웹 상에서 유저들의 거래 연결망을 그래프(자료구조) 형태로 보여주는 기능을 제작할 수 있음.
 
* 거래 API제공.
 - 플레이어A가 플레이어B와 거래할 때 자동 잔돈 지급 등의 돈 관리를 해주는 API제공.
 
* 복사 버그 방지
 - 플레이어가 정상적으로 발행되지 않은 돈을 획득할 경우 감지 가능.
 - 플레이어가 정상적으로 발행되지 않은 돈을 인벤토리에서 건드릴 경우 감지 가능.
 - 추적되지 않은 돈을 플레이어가 소유할 경우 일단 메인 시스템에서 발행된 돈이라고 간주. (기록: 시스템에서 발행됨 블록:창고, 좌표:xyz)(기록: 시스템에서 발행됨 좌표xyz)
 - 메인 시스템이 특정한 플레이어에게 돈을 비정상적으로 많이 발행해주었을 경우 그 플레이어는 버그 악용 유저로 판단
 
 

#돈의 상태 구분: 서버에 존재하는 아이템은 3가지의 상태를 가집니다.

* 블록에 종속됨: 돈이 특정 블록에 종속되어 NBT태그로 저장된 것을 말함(창고, 화로 등등)
 - 저장되는지? 예
  issue: 깔대기에 돈을 넣을경우 서버에 부하가 걸릴 수 있음 => 깔대기에 돈을 넣지 못하게 함.
  issue: 블록이 월드에딧 등으로 강제로 없어졌을 때는 추적 불가능 => 서버가 켜질 때마다 전체 블록을 검사하여 유효하지 않은 블록에 있는 돈은 삭제해줌.
   
* 아이템화된 상태: 돈 아이템이 필드에 개체로써 존재할때를 말함
 - 저장되는지? 아니오, DB에 저장하지 않고 HashMap<Item, EconomyObject> 형식으로 내부에서 관리함
   issue: 서버가 재부팅되면 필드에 엔티티로 존재하는 아이템(아이템화된 아이템)을 식별할 수 없음 => 서버가 켜질때 전체 맵의 엔티티를 조사하여 돈일 경우 해당 아이템을 삭제함.
 
* 플레이어에게 종속됨: 특정 플레이어가 돈을 자신의 인벤토리에 가지고 있는 상태를 말함
 - 저장되는지? 예
   issue: 플레이어 말고 다른 엔티티가 소유할 경우는 => 플레이어 말고 다른 앤티티는 돈을 소유할 수 없도록 함.
 
* 플러그인에게 종속됨:
   

# DB스키마

* 블록에 종속된 돈 
 - sql: 블록의 좌표(x,y,z)(int)(key), 돈의 총 액수(int), 업데이트시간(datetime), 플레이어UID(text)(key)
 - 특정 플레이어가 특정 좌표에 있는 블록에 돈을 얼마나 보관했는지 기록합니다.
 
* 플레이어에게 종속된 돈
 - sql:플레이어UID(text)(key), 돈의 총 액수(int), 업데이트시간(datetime)
 - 특정 플레이어가 인벤토리에 돈을 얼마나 가지고 있는지 기록합니다.
 
* 플레이어가 소유한 돈
 - sql:플레이어UID(text)(key), 돈의 총 액수(int)
 - 플레이어가 블록에 종속된 돈, 인벤토리에 보관한 돈을 포함하여 돈을 얼마나 가지고 있는지 보여줍니다.
 - 아이템화 된 상태인 돈은 보이지 않습니다.
 - 위 두 테이블 정보가 업데이트 될 경우 트리거를 사용해서 자동으로 업데이트합니다.
 
* 거래
 - sql: 거래코드(int)(key)(자동증가), 거래날짜(datetime)(index), 내 플레이어UID(text)(index), 상대 플레이어UID(text)(index), 이동한 가치(int), 내가 지불한 아이템JSON(varchar), 상대가 지불한 아이템JSON(varchar)
 - 모든 거래를 기록합니다.
 - 플레이어A와 플레이어B가 각각 창고에 100원, 120원을 보관했을 때 플레이어 A가 창고에서 200원을 꺼내면 플레이어 B가 플레이어A에게 100원을 거래한것으로 간주합니다.
 - 거래시마다 지속적으로 기록이 누적되며 지워도 시스템 동작에는 이상이 없습니다.
 - 내가 상대에게 돈을 줬을경우 이동한 가치에 양수를 기록, 반대일경우 음수를 기록합니다.
 
* 발급 및 소모 기록: 
 - sql: 발급코드(int)(key)(자동증가), 발급날짜(datetime)(index), 시행한 시스템(systemID)(index), 플레이어UID(index), 이동한 가치(int), 정보(varchar)
 - 화폐 발급 및 소모시마다 지속적으로 기록이 누적되며 지워도 시스템 동작에는 이상이 없습니다.
 - 플레이어가 아이템을 먹어야 합니다. (발급되서 필드에 아이템 엔티티로 존재하다 사라지는 경우 기록되지 않습니다)
 - 해당 행위에 대한 자세한 정보가 기록될 수 있습니다. (섬 관리 모듈에서 섬을 1단계만큼 확장했습니다.)
 
* 소유권 이전 기록:
 - sql: 소유권 이전 코드(int)(key)(자동증가), 획득날짜(datetime)(index), 내 속성(Player|System), 대상 속성(Player|System), 나(text)(index), 대상(text)(index), 이동한 가치(int)
 - 모든 소유권 이전 기록을 기록합니다(거래, 발급받음, 소모된)
 - 거래, 발급 및 소모 기록 테이블에 트리거를 사용해서 자동으로 업데이트합니다.
 - 지속적으로 누적되며 지워도 시스템 동작에는 이상이 없습니다.
  
  
#주요 클래스

* EconomyModule: 모든 EconomyObject를 관리하는 클래스
 - 필드:
  1. private ArrayList<EconomySystemItemObject> systemMoneyObjects: 필드에 떨어진 시스템이 발급한 돈
  2. private ArrayList<EconomyPlayerItemObject> playerMoneyObjects: 필드에 떨어진 플레이어 소유인 돈
  3. private ArrayList<EconomyBlockInfo> blockMoneyObjects: DB플레이어에게 종속된 돈에게 매칭
  4. private ArrayList<EconomyPlayerInfo> playerMoneyObject: DB플레이어가 소유한 돈에게 매칭
  5. private HashMap<Player, int> playerMoney: 플레이어가 소유한 돈입니다.
   playerMoneyObjects, blockMoneyObjects, playerMoneyObject에서 특정 플레이어에 해당하는 값을 전부 더한 것.
   이 값을 기반으로 이상 현상(돈 복사 등)을 감지합니다.
  6. private Queue<EconomyBlockInfo> blockDBQueue: TPS드랍 방지 위해 DB삽입 큐를 따로 둔다
  7. private Queue<EconomyPlayerInfo> playerDBQueue: TPS드랍 방지 위해 DB삽입 큐를 따로 둔다
 - 메서드:
  1. 필드에 대한 getter 메소드: 
   ArrayList의 경우 내부 데이터를 수정할 수 없는 상태로 반환됩니다.
  2. TradeResult trade(myPlayer, targetPlayer, myItemStack, targetItemStack, movedValue):
   해당 플레이어와 거래를 시도합니다. 자동으로 거스름돈을 생성해줍니다.
   파라미터: 순서대로 내 플레이어, 상대방, 내가 제시한 아이템, 상대가 제시한 아이템, 나에게서 상대에게 이동할 가치 입니다.
   nullable한 파라미터: myItemStack, targetItemStack
   issue: 물물교환이 활성화 될 경우 서버 돈의 흐름 추적이 힘들어질 수 있음.
  3. int getPlayerMoney(Player player): 플레이어가 소유한 돈을 반환합니다.
  4. EconomySystemMoney createMoney(String systemID, int value, [String info])
   시스템이 돈을 발행합니다.
   value에 1250을 넣었을 경우 금화 하나, 은화 하나, 동화 다섯개가 반환됩니다.
   분석을 용이하게 하기 위해 설명을 작성할 수 있습니다.
  5. ConsumeResult consumeMoney(Player player, String systemID, int value, [String info])
   시스템이 특정 플레이어에게서 돈을 회수합니다.
   분석을 용이하게 하기 위해 설명을 작성할 수 있습니다.
   플레이어의 인벤토리에 그만큼의 돈이 없거나 잔돈을 회수할 인벤토리가 없을 경우 해당하는 오류가 반환됩니다.
  6. ItemStack getCoinData(CoinType type)
   샘플로 사용하기 위해 동전 정보를 가져옵니다.(유통할 수 없음)

* EconomyBlockInfo: DB 블록에 종속된 돈의 한 인스턴스에 매칭되는 클래스
* EconomyPlayerInfo: DB 플레이어에 종속된 돈의 한 인스턴스에 매칭되는 클래스

* TradeResult: 돈 거래에 결과를 담고있는 불변 클래스
 - 필드:
  1. TradeResultType resultType(거래 성공, 거래 실패(내 플레이어 인벤토리부족), 거래 실패(타겟 플레이어 인벤토리 부족), ...)
  2. private int myCost;
  3. private int targetCost;
  4. private ArrayList<ItemStack> myItemStack;
  5. private ArrayList<ItemStack> targetItemStack;
  ....
 - 메소드:
  1. 필드에 대한 getter메소드
  
* EconomySystemMoney: 플러그인에 종속된 돈을 관리하는 클래스
 - 필드:
  1. private int value;
  2. private String systemID: 발행자 아이디(상점 시스템, 로그라이크 시스템 등)
  3. private Player owner
  4. private Location location
 - 메서드:
  1. 필드에 대한 getter 메소드
  2. Location getLoc(): 돈의 현재 좌표를 반환합니다.
  3. String getCreaterID(): 돈의 발행자를 반환합니다.
  4. Player getOwner(): 돈의 소유자를 반환합니다.(소유 플레이어가 없을 경우 null)

* private EconomySystemItemObject: 맵에 아이템화된 시스템이 발행한 돈에 대한 정보를 담는 클래스
 - 필드:
  1. private ItemStack items
  2. private int value
  3. private String systemID: 발행자 아이디(상점 시스템, 로그라이크 시스템 등)
  4. private Location location
 - 메서드:
  1. 필드에 대한 getter 메소드
  2. Location getLoc(): 돈의 현재 좌표를 반환합니다.
  3. int getTotalPrice(): 전체 가치를 반환합니다. (가치가 1000인 금화가 12개 있으면 12000반환)
  4. String getCreaterID(): 돈의 발행자를 반환합니다.
 issue: ArrayList에 담겨있는 ItemStack이 전부 땅에서 없어졌을때(플레이어가 먹거나, 사라졌을때) DB에 저장하도록 구현하여야 함.

* private EconomyPlayerItemObject: 맵에 아이템화된 플레이어가 발행한 돈에 대한 정보를 담는 클래스
 - 필드:
  1. private ItemStack items
  2. private int value
  3. private Player player: 발행자 플레이어
  4. private Location location
 - 메서드:
  1. 필드에 대한 getter 메소드
  2. Location getLoc(): 돈의 현재 좌표를 반환합니다.
  3. int getTotalPrice(): 전체 가치를 반환합니다. (가치가 1000인 금화가 12개 있으면 12000반환)
  4. Player getOwner(): 돈의 소유자를 반환합니다.


# 구현 이슈

* 서버와 DB간 비동기화가 발생할 수 있음.
 - 처음 서버가 구동될때 DB에 저장된 모든 객체에 대한 유효성 검사
 - 플레이어가 더 많은 돈을 소지하고 있을 경우 메인 시스템이 돈을 발행하여 플레이어에게 지불했다고 처리, 이유는 데이터베이스 불일치로 작성
 - 플레이어가 더 적은 돈을 소지하고 있을 경우 플레이어가 돈을 메인 시스템에 소모했다고 처리, 이유는 데이터베이스 불일치로 작성
