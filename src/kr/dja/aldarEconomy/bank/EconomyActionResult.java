package kr.dja.aldarEconomy.bank;

public enum EconomyActionResult
{
	insufficientSpace,// 인벤토리 공간부족
	insufficientMoney,// 인벤토리에 충분한 돈이 있지 않음
	unitTooSmall,// 단위가 돈의 최소 단위보다 작음. ex) 1234원 입력, 10원이 최소액권이면 발생
	OK// 완료
}
