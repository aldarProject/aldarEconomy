package kr.dja.aldarEconomy.api;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Container;
import org.bukkit.entity.HumanEntity;

import kr.dja.aldarEconomy.api.token.SystemID;
import kr.dja.aldarEconomy.storage.MoneyDetailResult;

public interface AldarEconomy
{
	/*
	 * 
	 * 먼저 plugin.yml에서 AldarEconomy에 대한 depend를 추가하셔야 합니다.
	 * https://bukkit.gamepedia.com/Plugin_YAML 참고
	 * 플러그인이 onload될때 getServer().getPluginManager().getPlugin("AldarEconomy")
	 * 로 플러그인 인스턴스를 확보하신 후 AldarEconomyCore로 형변환 하신다음 getEconomyModule()로
	 * API인터페이스를 획득하세요.
	 * 그다음 takeAPIToken(플러그인이름)으로 APIToken을 발급받으신 후, 아래 메소드들을 이용하시면 됩니다
	 * 
	 * 파라미터 설명: cause는 왜 해당 행위를 하는지, args는 해당 행위에 대한 자세한 정보입니다
	 * 두 파라미터 전부 nullable합니다. 필요없으면 null넣어주시는데 앞에 cause는 어지간하면 적어주세요.
	 * 예를들어서 거래 플러그인을 제작한다 하면, cause에는 "trade", args에는 거래한 물품에 대한 json정보를 담을 수 있습니다.
	 * 
	 * cause가 같으면 args도 같은 포맷으로 해주세요, 같은cause인데 args가 어떤건 CSV포맷, 어떤건 XML, 어떤건 json이면
	 * 추후에 해당 정보를 파싱하기 난감합니다.
	 * 
	 * args는 gson라이브러리를 사용하시어 json형식으로 넘겨주시길 권장합니다.
	 */
	
	public int getPlayerInventoryMoney(OfflinePlayer player);
	public MoneyDetailResult getPlayerMoneyDetail(OfflinePlayer player);
	
	public SystemID takeAPIToken(String name);
	
	public EconomyResult depositPlayer(HumanEntity player, int amount, SystemID system, String cause, String args);
	public EconomyResult withdrawPlayer(HumanEntity player, int amount, SystemID system, String cause, String args);
	
	public EconomyResult depositChest(Container container, int amount, SystemID system, String cause, String args);
	public EconomyResult withdrawChest(Container container, int amount, SystemID system, String cause, String args);
	public EconomyResult depositItem(Location item, int amount, SystemID system, String cause, String args);
	
	public EconomyResult playerToChest(HumanEntity player, Container containr, int amount, SystemID system, String cause, String args);
	public EconomyResult chestToPlayer(Container container, HumanEntity player, int amount, SystemID system, String cause, String args);
	public EconomyResult playerToPlayer(HumanEntity source, HumanEntity target, int amount, SystemID system, String cause, String args);
	
	public String economyFormat(int economy);
}
