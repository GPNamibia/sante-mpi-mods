using System;
using SanteDB.BusinessRules.JavaScript;
using SanteDB.BusinessRules.JavaScript.Exceptions;
using SanteDB.Core.Diagnostics;
using SanteDB.Core.Services;
using SanteDB.Persistence.Data.ADO.Configuration;
using Newtonsoft.Json;
using System.Text;

namespace SanteHealthID
{
    public class SanteHealthIDGeneration
    {
        private readonly AdoPersistenceConfigurationSection _configuration;
        private String codePoints = "123456789ACDEFGHJKLMNPRTUVWXY";
        private readonly Tracer _tracer = Tracer.GetTracer(typeof(SanteHealthIDGeneration));

        public SanteHealthIDGeneration(IConfigurationManager configurationManager)
        {
            this._configuration = configurationManager.GetSection<AdoPersistenceConfigurationSection>();
            // This will add SanteHealthIDRuleObject to the JavaScript context on all executors
            // The scripts will be able to access this object via SanteHealthIDRuleObject.
            JavascriptExecutorPool.Current.ExecuteGlobal(o => o.AddExposedObject("SanteHealthIDRuleObject", this));
        }

        public int CodePointFromCharacter(char character)
        {
            return codePoints.IndexOf(character);
        }

        public char CharacterFromCodePoint(int codePoint)
        {
            return codePoints[codePoint];
        }

        public int NumberOfValidInputCharacters()
        {
            return codePoints.Length;
        }

        public char GenerateCheckCharacter(string input)
        {
            int factor = 2;
            int sum = 0;
            int n = NumberOfValidInputCharacters();

            // Starting from the right and working leftwards is easier since 
            // the initial "factor" will always be "2".
            for (int i = input.Length - 1; i >= 0; i--)
            {
                int codePoint = CodePointFromCharacter(input[i]);
                int addend = factor * codePoint;

                // Alternate the "factor" that each "codePoint" is multiplied by
                factor = (factor == 2) ? 1 : 2;

                // Sum the digits of the "addend" as expressed in base "n"
                addend = (addend / n) + (addend % n);
                sum += addend;
            }

            // Calculate the number that must be added to the "sum" 
            // to make it divisible by "n".
            int remainder = sum % n;
            int checkCodePoint = (n - remainder) % n;

            return CharacterFromCodePoint(checkCodePoint);
        }

        public bool ValidateCheckCharacter(string input)
        {
            int factor = 1;
            int sum = 0;
            int n = NumberOfValidInputCharacters();

            // Starting from the right, work leftwards
            // Now, the initial "factor" will always be "1" 
            // since the last character is the check character.
            for (int i = input.Length - 1; i >= 0; i--)
            {
                int codePoint = CodePointFromCharacter(input[i]);
                int addend = factor * codePoint;

                // Alternate the "factor" that each "codePoint" is multiplied by
                factor = (factor == 2) ? 1 : 2;

                // Sum the digits of the "addend" as expressed in base "n"
                addend = (addend / n) + (addend % n);
                sum += addend;
            }

            int remainder = sum % n;

            return (remainder == 0);
        }

        public static String convertToBase(long n, char[] baseCharacters, int padToLength)
        {
            StringBuilder sb = new StringBuilder();
            long numInBase = (long)baseCharacters.Length;
            while (n > 0)
            {
                int index = (int)(n % numInBase);
                sb.Insert(0, baseCharacters[index]);
                n = (long)(n / numInBase);
            }

            while (sb.Length < padToLength)
            {
                sb.Insert(0, baseCharacters[0]);
            }
            return sb.ToString();
        }

        /// <summary>
        /// Javascript will be able to call SanteHealthIDRuleObject.GetNextSequenceValue("my_sequence_name");
        /// </summary>
        public object GetNextSequenceValue(String sequenceName)
        {
            if (String.IsNullOrEmpty(sequenceName))
            {
                throw new ArgumentNullException(nameof(sequenceName));
            }

            try
            {
                using (var context = this._configuration.Provider.GetWriteConnection())
                {
                    context.Open();
                    var sqlStatement = context.CreateSqlStatement("SELECT nextval(?)", sequenceName);
                    long sequence_val = context.ExecuteScalar<long>(sqlStatement);
                    Console.WriteLine("Sequence value is {0}", sequence_val);
                    var padToLength = 6;

                    char[] baseCharacters = { '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K', 'L', 'M', 'N', 'P', 'R', 'T', 'U', 'V', 'W', 'X', 'Y' };

                    var cv_ret = convertToBase(sequence_val, baseCharacters, padToLength);
                    var ch_digit = GenerateCheckCharacter(cv_ret);
                    var health_id = cv_ret + ch_digit;
                    Console.WriteLine("Health ID is {0}", health_id);
                    return health_id;
                }
            }
            catch (Exception e)
            {
                this._tracer.TraceError("Error getting sequence value {0}", e);
                throw new JsBusinessRuleException("Error getting sequence value", e);
            }
        }
    }

}