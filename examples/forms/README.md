# Banking Form Schema Examples

This directory contains example JSON Schema Draft 7 definitions for common banking forms. These schemas can be used with the Phase 7 Dynamic Forms feature in ProcessMonster.

## Available Forms

### 1. Loan Application Form
**File:** `loan-application-schema.json`

**Purpose:** Personal loan application processing

**Key Features:**
- Personal information (name, contact, SSN, DOB)
- Employment and income details
- Loan details (amount, purpose, term)
- Address verification
- Optional co-applicant support
- Credit score estimation
- Terms and consent checkboxes

**Validations:**
- Email format
- Phone number pattern (10 digits)
- SSN pattern (9 digits)
- Loan amount: $1,000 - $500,000
- Loan terms: 12-84 months
- State code: 2 uppercase letters
- ZIP code: 5 digits
- Required consent checkboxes

---

### 2. Account Opening Form
**File:** `account-opening-schema.json`

**Purpose:** New bank account creation (checking, savings, money market, CD)

**Key Features:**
- Account type selection
- Account ownership (individual/joint/business)
- Personal identification (SSN, government ID)
- Citizenship verification
- Address information (physical + mailing)
- Employment information
- Initial deposit details
- Service preferences (debit card, checks, online banking)
- Paperless options

**Validations:**
- Required minimum deposit: $25
- ID expiration date validation
- Employment status enum
- Funding source options
- Terms and privacy policy consent

---

### 3. Wire Transfer Request Form
**File:** `wire-transfer-schema.json`

**Purpose:** Domestic and international wire transfer initiation

**Key Features:**
- Transfer type (domestic/international)
- Multi-currency support (USD, EUR, GBP, CAD, etc.)
- Sender account information
- Beneficiary details (name, account, bank)
- SWIFT/BIC and routing number support
- Intermediary bank option
- Transfer purpose and description
- Urgency levels (standard/urgent/same-day)
- Scheduled transfer option
- AML/KYC compliance fields (source of funds, relationship)

**Validations:**
- SWIFT code pattern validation
- Routing number (ABA) pattern (9 digits)
- Transfer amount: $1 - $1,000,000
- Conditional requirements based on transfer type:
  - **International:** Requires SWIFT code and beneficiary address
  - **Domestic:** Requires routing number
- Required compliance disclosures

---

## JSON Schema Features Used

All forms demonstrate various JSON Schema Draft 7 features:

### Data Types
- `string` - Text fields with various formats
- `number` - Monetary amounts, decimal values
- `integer` - Whole numbers (e.g., loan terms, check quantities)
- `boolean` - Checkboxes and toggles
- `object` - Nested structures (e.g., co-applicant info)

### Validation Rules
- `required` - Required fields
- `minLength` / `maxLength` - String length constraints
- `minimum` / `maximum` - Numeric bounds
- `pattern` - Regex patterns (SSN, phone, ZIP, SWIFT)
- `format` - Built-in formats (email, date, date-time, uri)
- `enum` - Fixed set of options (dropdowns)
- `const` - Constant values (e.g., must agree to terms)

### Advanced Features
- **Conditional Logic:** Wire transfer form uses `if`/`then`/`else` for conditional requirements
- **Nested Objects:** Loan application includes co-applicant sub-object
- **Descriptions:** Helpful hints for users
- **Defaults:** Pre-filled values for common selections
- **ReadOnly:** Display-only fields (e.g., estimated fees)

---

## How to Use These Schemas

### 1. Import via API

```bash
curl -X POST http://localhost:8080/api/v1/forms/definitions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d @loan-application-request.json
```

Where `loan-application-request.json` contains:

```json
{
  "formKey": "loan-application",
  "name": "Loan Application Form",
  "description": "Form for personal loan applications",
  "category": "LOAN",
  "schemaJson": "<paste the schema here>",
  "published": false
}
```

### 2. Validate and Publish

```bash
# Validate the schema
curl -X POST http://localhost:8080/api/v1/forms/definitions/validate-schema \
  -H "Content-Type: application/json" \
  -d @loan-application-schema.json

# Publish the form
curl -X PUT http://localhost:8080/api/v1/forms/definitions/{id}/publish \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 3. Submit Form Data

Example submission for loan application:

```json
{
  "formDefinitionId": 1,
  "dataJson": "{\"applicantType\":\"individual\",\"firstName\":\"John\",\"lastName\":\"Doe\",\"email\":\"john.doe@example.com\",\"phone\":\"5551234567\",\"dateOfBirth\":\"1985-03-15\",\"ssn\":\"123456789\",\"employmentStatus\":\"employed\",\"employer\":\"Acme Corp\",\"jobTitle\":\"Software Engineer\",\"annualIncome\":95000,\"loanAmount\":25000,\"loanPurpose\":\"debt-consolidation\",\"loanTerm\":48,\"addressLine1\":\"123 Main St\",\"city\":\"Boston\",\"state\":\"MA\",\"zipCode\":\"02101\",\"creditScore\":750,\"hasCoApplicant\":false,\"agreedToTerms\":true,\"consentToCreditCheck\":true}",
  "businessKey": "LOAN-2025-001"
}
```

---

## Customization Guidelines

### Adding New Fields

1. Add to `properties` object
2. Specify `type` and `title`
3. Add validation rules (`minLength`, `pattern`, etc.)
4. Add to `required` array if mandatory
5. Add description for user guidance

### Example: Adding Mother's Maiden Name

```json
"mothersMaidenName": {
  "type": "string",
  "title": "Mother's Maiden Name",
  "minLength": 1,
  "maxLength": 50,
  "description": "For security verification purposes"
}
```

### Creating Conditional Fields

Use `if`/`then`/`else` for conditional requirements:

```json
"if": {
  "properties": { "hasCoApplicant": { "const": true } }
},
"then": {
  "required": ["coApplicant"]
}
```

---

## Best Practices

1. **Required Fields:** Only mark truly essential fields as required
2. **Validation:** Use appropriate patterns and constraints
3. **User Experience:** Provide clear descriptions and defaults
4. **Privacy:** Mark sensitive fields appropriately
5. **Testing:** Always validate schema before publishing
6. **Versioning:** Create new version when making breaking changes

---

## Compliance Considerations

These forms include fields for regulatory compliance:

- **KYC (Know Your Customer):** SSN, ID verification, address
- **AML (Anti-Money Laundering):** Source of funds, relationship to beneficiary
- **PATRIOT Act:** Customer identification program fields
- **FCRA (Fair Credit Reporting Act):** Credit check consent
- **E-Sign Act:** Electronic signature consent

Ensure your implementation:
- Encrypts sensitive data (SSN, account numbers)
- Maintains audit trails
- Implements proper access controls
- Complies with data retention policies
- Follows PCI DSS for payment card data

---

## Related Documentation

- [JSON Schema Specification](https://json-schema.org/draft-07/schema)
- [ProcessMonster API Documentation](../docs/API.md)
- [Dynamic Forms User Guide](../docs/FORMS.md)

---

**Last Updated:** 2025-11-07
**Version:** 1.0
**Maintained by:** ProcessMonster Team
