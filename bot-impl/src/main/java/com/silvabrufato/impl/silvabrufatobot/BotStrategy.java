package com.silvabrufato.impl.silvabrufatobot;

import com.bueno.spi.model.*;
import com.bueno.spi.model.GameIntel.RoundResult;
import com.silvabrufato.impl.silvabrufatobot.BotBluff.Probability;

import java.util.ArrayList;
import java.util.List;

public enum BotStrategy {

    FIRST_ROUND_STRATEGY {
        @Override
        public CardToPlay throwCard(GameIntel gameIntel) {
            if (isOpponentThatStartTheRound(gameIntel))
                return chooseCardToWinOrToDrawTheRoundIfPossible(gameIntel, false);
            return chooseTheLowestManilhaIfHaveOneOrTheLowestCardToPlay(gameIntel, false);
        }

        @Override
        public int responseToRaisePoints(GameIntel gameIntel) {
            if (countManilhas(gameIntel) >= 2 || hasZapAndThree(gameIntel) ||
                hasCopasAndThree(gameIntel))
                return 0;
            return -1;
        }

        @Override
        public boolean raisePoints(GameIntel gameIntel) {
            if (countManilhas(gameIntel) >= 2 && hasZap(gameIntel))
                return true;
            if (countManilhas(gameIntel) >= 1 && hasZap(gameIntel))
                return BotBluff.of(Probability.P60).bluff();
            if (countManilhas(gameIntel) >= 1)
                return BotBluff.of(Probability.P40).bluff();
            if (countThree(gameIntel) >= 2 || countCardsEqualOrHigherThanAce(gameIntel) >= 2)
                return BotBluff.of(Probability.P20).bluff();
            return false;
        }
    },

    SECOND_ROUND_STRATEGY {
        @Override
        public CardToPlay throwCard(GameIntel gameIntel) {
            if (drewThePreviousRound(gameIntel)) {
                if (isOpponentThatStartTheRound(gameIntel))
                    return chooseCardToWinTheRoundIfPossible(gameIntel, false);
                return chooseTheStrongestManilhaIfHaveOneOrTheLowesCardToPlay(gameIntel, true);
            }
            if (isOpponentThatStartTheRound(gameIntel))
                return chooseCardToWinTheRoundIfPossible(gameIntel, true);
            return chooseTheLowestManilhaIfHaveOneOrTheLowestCardToPlay(gameIntel, false);
        }

        @Override
        public int responseToRaisePoints(GameIntel gameIntel) {
            if (gameIntel.getRoundResults().get(0) == RoundResult.LOST) {
                if (hasCopasAndEspadilha(gameIntel))
                    return 1;
                if (hasCopasAndOuros(gameIntel))
                    return 0;
            }
            if (gameIntel.getRoundResults().get(0) == RoundResult.WON) {
                if (hasCopas(gameIntel) || countManilhas(gameIntel) >= 2)
                    return 1;
                if (hasManilhaAndThree(gameIntel) || countThree(gameIntel) == 2)
                    return 0;
            }
            return -1;
        }

        @Override
        public boolean raisePoints(GameIntel gameIntel) {
            if (gameIntel.getRoundResults().get(0) == RoundResult.LOST) {
                if (BotStrategy.hasCopas(gameIntel))
                    return true;
                if (BotStrategy.countManilhas(gameIntel) == 2)
                    return true;
                if (cardGreaterThaAndAnotherCardGreaterThanOrEqualToTheAce(gameIntel))
                    return true;
            } else {
                if (BotStrategy.hasZap(gameIntel))
                    return true;
                if (BotStrategy.hasCopas(gameIntel))
                    return true;
                if (BotStrategy.hasEspadilha(gameIntel))
                    return true;
                if (BotStrategy.countCardsEqualOrHigherThanAce(gameIntel) == 2)
                    return true;
            }

            // if(gameIntel.getRoundResults().get(0) == RoundResult.WON) {
            // if(countManilhas(gameIntel) >= 1) return true;
            // return BotBluff.of(Probability.P40).bluff();
            // }
            // return BotBluff.of(Probability.P20).bluff();
            return false;
        }
    },

    THIRD_ROUND_STRATEGY {
        @Override
        public CardToPlay throwCard(GameIntel gameIntel) {
            return SECOND_ROUND_STRATEGY.throwCard(gameIntel);
        }

        @Override
        public int responseToRaisePoints(GameIntel gameIntel) {
            /*
             * if (gameIntel.getRoundResults().get(0) == RoundResult.LOST) {
             * if (hasEspadilha(gameIntel) || hasOuros(gameIntel))
             * return 0;
             * }
             * if (gameIntel.getRoundResults().get(0) == RoundResult.WON) {
             * if (hasEspadilha(gameIntel) || hasOuros(gameIntel) || countThree(gameIntel) >
             * 0)
             * return 0;
             * }
             */
            return -1;
        }

        @Override
        public boolean raisePoints(GameIntel gameIntel) {
            if (BotStrategy.hasEspadilha(gameIntel))
                return true;
            if (BotStrategy.hasOuros(gameIntel))
                return true;
            return false;
        }
    };

    private static boolean cardGreaterThaAndAnotherCardGreaterThanOrEqualToTheAce(GameIntel gameIntel) {
        List<TrucoCard> cards = sortCards(gameIntel);

        if (cards.get(0).compareValueTo(gameIntel.getOpponentCard().get(), gameIntel.getVira()) > 0) {
            if (cards.get(1).getRank().value() == 10 || cards.get(1).getRank().value() == 9
                    || cards.get(1).getRank().value() == 8)
                return true;
        }
        if (cards.get(1).compareValueTo(gameIntel.getOpponentCard().get(), gameIntel.getVira()) > 0) {
            if (cards.get(0).getRank().value() == 10 || cards.get(0).getRank().value() == 9
                    || cards.get(0).getRank().value() == 8)
                return true;
        }

        return false;
    }

    private static boolean drewThePreviousRound(GameIntel gameIntel) {
        return gameIntel.getRoundResults().get(gameIntel.getRoundResults().size() - 1) == RoundResult.DREW;
    }

    private static List<TrucoCard> sortCards(GameIntel gameIntel) {
        ArrayList<TrucoCard> arrayOfCards = new ArrayList<>();
        arrayOfCards.addAll(gameIntel.getCards());
        arrayOfCards.sort((card1, card2) -> card1.compareValueTo(card2, gameIntel.getVira()));
        return List.copyOf(arrayOfCards);
    }

    private static boolean isOpponentThatStartTheRound(GameIntel gameIntel) {
        return gameIntel.getOpponentCard().isPresent();
    }

    private static CardToPlay chooseTheLowestCardToPlay(GameIntel gameIntel, boolean discard) {
        List<TrucoCard> cards = sortCards(gameIntel);
        if (discard)
            return CardToPlay.discard(cards.get(0));
        return CardToPlay.of(cards.get(0));
    }

    private static CardToPlay chooseCardToWinOrToDrawTheRoundIfPossible(GameIntel gameIntel, boolean discard) {
        List<TrucoCard> cards = sortCards(gameIntel);
        for (TrucoCard card : cards)
            if (card.compareValueTo(gameIntel.getOpponentCard().get(), gameIntel.getVira()) > 0)
                return CardToPlay.of(card);
        for (TrucoCard card : cards)
            if (card.compareValueTo(gameIntel.getOpponentCard().get(), gameIntel.getVira()) == 0)
                return CardToPlay.of(card);
        return chooseTheLowestCardToPlay(gameIntel, discard);
    }

    private static CardToPlay chooseCardToWinTheRoundIfPossible(GameIntel gameIntel, boolean discard) {
        for (TrucoCard card : gameIntel.getCards())
            if (card.compareValueTo(gameIntel.getOpponentCard().get(), gameIntel.getVira()) > 0)
                return CardToPlay.of(card);
        return chooseTheLowestCardToPlay(gameIntel, discard);
    }

    private static CardToPlay chooseTheStrongestManilhaIfHaveOneOrTheLowesCardToPlay(GameIntel gameIntel,
            boolean discard) {
        if (countManilhas(gameIntel) > 0) {
            List<TrucoCard> cards = sortCards(gameIntel);
            return CardToPlay.of(cards.get(cards.size() - 1));
        }
        return chooseTheLowestCardToPlay(gameIntel, discard);
    }

    private static CardToPlay chooseTheLowestManilhaIfHaveOneOrTheLowestCardToPlay(GameIntel gameIntel,
            boolean discard) {
        List<TrucoCard> cards = sortCards(gameIntel);
        for (TrucoCard card : cards)
            if (card.isManilha(gameIntel.getVira()))
                return CardToPlay.of(card);
        return chooseTheLowestCardToPlay(gameIntel, discard);
    }

    public static int countManilhas(GameIntel gameIntel) {
        int count = 0;
        for (TrucoCard card : gameIntel.getCards())
            if (card.isManilha(gameIntel.getVira()))
                count++;
        return count;
    }

    public static int countThree(GameIntel gameIntel) {
        int count = 0;
        for (TrucoCard card : gameIntel.getCards())
            if (card.getRank().value() == CardRank.THREE.value())
                count++;
        return count;
    }

    public static int countCardsEqualOrHigherThanAce(GameIntel gameIntel) {
        int count = 0;
        for (TrucoCard card : gameIntel.getCards())
            if (card.getRank().value() >= CardRank.ACE.value())
                count++;
        return count;
    }

    private static boolean hasZap(GameIntel gameIntel) {
        for (TrucoCard card : gameIntel.getCards()) {
            if (card.isZap(gameIntel.getVira()))
                return true;
        }
        return false;
    }

    private static boolean hasCopas(GameIntel gameIntel) {
        for (TrucoCard card : gameIntel.getCards()) {
            if (card.isCopas(gameIntel.getVira()))
                return true;
        }
        return false;
    }

    private static boolean hasEspadilha(GameIntel gameIntel) {
        for (TrucoCard card : gameIntel.getCards()) {
            if (card.isEspadilha(gameIntel.getVira()))
                return true;
        }
        return false;
    }

    private static boolean hasOuros(GameIntel gameIntel) {
        for (TrucoCard card : gameIntel.getCards()) {
            if (card.isOuros(gameIntel.getVira()))
                return true;
        }
        return false;
    }

    private static boolean hasZapAndThree(GameIntel gameIntel) {
        return hasZap(gameIntel) && countThree(gameIntel) > 0;
    }

    private static boolean hasCopasAndThree(GameIntel gameIntel) {
        return hasCopas(gameIntel) && countThree(gameIntel) > 0;
    }

    private static boolean hasCopasAndEspadilha(GameIntel gameIntel) {
        return hasCopas(gameIntel) && hasEspadilha(gameIntel);
    }

    private static boolean hasCopasAndOuros(GameIntel gameIntel) {
        return hasCopas(gameIntel) && hasOuros(gameIntel);
    }

    private static boolean hasManilhaAndThree(GameIntel gameIntel) {
        return countManilhas(gameIntel) > 0 && countThree(gameIntel) > 0;
    }

    public abstract CardToPlay throwCard(GameIntel gameIntel);

    public abstract int responseToRaisePoints(GameIntel gameIntel);

    public abstract boolean raisePoints(GameIntel gameIntel);

}
